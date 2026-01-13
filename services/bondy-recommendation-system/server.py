# server.py (fixed - robust vector parsing + consistent DB writes)
import os
import ast
import threading
import time
from collections import defaultdict

from dotenv import load_dotenv
from fastapi import FastAPI, Query
from pydantic import BaseModel
import numpy as np
from sklearn.feature_extraction.text import TfidfVectorizer
import psycopg2
from psycopg2.extras import RealDictCursor
from cachetools import TTLCache

load_dotenv()

app = FastAPI(title="Bondy Recommendation Service (fixed vectors)")

# ---------------------------
# CONFIG
# ---------------------------
DB_CONFIG = {
    "host": os.getenv("DB_HOST", "localhost"),
    "port": int(os.getenv("DB_PORT", 5432)),
    "dbname": os.getenv("DB_NAME", "postgres"),
    "user": os.getenv("DB_USER", "postgres"),
    "password": os.getenv("DB_PASS", ""),
}

TOP_N = int(os.getenv("TOP_N", 20))
REFIT_INTERVAL_SECONDS = int(os.getenv("REFIT_INTERVAL_SECONDS", 600))
TARGET_DIM = int(os.getenv("TARGET_DIM", 500))

# Weights
REACT_WEIGHT = float(os.getenv("REACT_WEIGHT", 1.0))
COMMENT_WEIGHT = float(os.getenv("COMMENT_WEIGHT", 1.2))
SHARE_WEIGHT = float(os.getenv("SHARE_WEIGHT", 1.5))
READ_WEIGHT = float(os.getenv("READ_WEIGHT", 0.5))

ALPHA = float(os.getenv("ALPHA", 0.8))

# cache
recommend_cache = TTLCache(maxsize=10000, ttl=300)

# TF-IDF
vectorizer = TfidfVectorizer(max_features=min(500, TARGET_DIM), stop_words="english")


# ---------------------------
# DB helpers
# ---------------------------
def get_conn():
    return psycopg2.connect(cursor_factory=RealDictCursor, **DB_CONFIG)


def _vec_to_literal(vec_iterable):
    """Convert iterable of numbers -> pgvector literal string like '[0.1,0.2,...]'"""
    # use high precision string formatting but avoid scientific notation
    return "[" + ",".join(f"{float(x):.17g}" for x in vec_iterable) + "]"


def parse_db_vector(value):
    """
    Robustly parse vector values coming from DB to numpy array(dtype=float).
    Accepts:
      - None -> None
      - list/tuple of numbers -> np.array
      - string like "[0,0,0]" -> ast.literal_eval -> np.array
      - numpy array -> ensure dtype float
    """
    if value is None:
        return None
    if isinstance(value, np.ndarray):
        return value.astype(float)
    if isinstance(value, (list, tuple)):
        # elements may be strings or numbers
        try:
            return np.array([float(x) for x in value], dtype=float)
        except Exception:
            # fallback: try ast on joined string elements
            try:
                s = "[" + ",".join(map(str, value)) + "]"
                parsed = ast.literal_eval(s)
                return np.array([float(x) for x in parsed], dtype=float)
            except Exception:
                return None
    if isinstance(value, (bytes, bytearray)):
        # decode and parse
        try:
            s = value.decode()
            parsed = ast.literal_eval(s)
            return np.array([float(x) for x in parsed], dtype=float)
        except Exception:
            return None
    if isinstance(value, str):
        # common: value comes as string "[0,0,...]"
        try:
            parsed = ast.literal_eval(value)
            return np.array([float(x) for x in parsed], dtype=float)
        except Exception:
            # maybe comma-separated without brackets
            try:
                parts = [p for p in value.split(",") if p.strip() != ""]
                return np.array([float(x) for x in parts], dtype=float)
            except Exception:
                return None
    # unknown type
    try:
        return np.array(value, dtype=float)
    except Exception:
        return None


def pad_to_target(arr2d, target=TARGET_DIM):
    """Pad 2D numpy array (n x d) to width target by zeros (or truncate)."""
    n, d = arr2d.shape
    if d == target:
        return arr2d
    if d > target:
        return arr2d[:, :target]
    pad = np.zeros((n, target - d), dtype=float)
    return np.hstack((arr2d, pad))


# ---------------------------
# Refit / rebuild vectors & profiles
# ---------------------------
def load_posts_content():
    conn = get_conn()
    cur = conn.cursor()
    try:
        cur.execute(
            """
            SELECT p.id,
                   COALESCE(p.content_text, '') || COALESCE(' ' || sp.content_text, '') AS content_text
            FROM posts p
            LEFT JOIN posts sp ON p.shared_from_post_id = sp.id
            ORDER BY p.id
            """
        )
        rows = cur.fetchall()
        if not rows:
            return [], []
        post_ids = [int(r["id"]) for r in rows]
        contents = [r["content_text"] or "" for r in rows]
        return post_ids, contents
    finally:
        cur.close()
        conn.close()


def refit_vectors():
    global vectorizer
    print("[INFO] Starting full refit...")

    post_ids, contents = load_posts_content()
    if not post_ids:
        print("[INFO] No posts found for refit")
        return

    # fit TF-IDF
    try:
        X_sparse = vectorizer.fit_transform(contents)
        X = X_sparse.toarray()
    except Exception as e:
        print(f"[ERROR] TF-IDF fit_transform failed: {e}")
        return

    X = pad_to_target(np.array(X, dtype=float), TARGET_DIM)

    conn = get_conn()
    cur = conn.cursor()
    try:
        # UPSERT post_vectors using literal ::vector to ensure pgvector type
        inserted = 0
        for pid, vec in zip(post_ids, X.tolist()):
            # skip all-zero vectors to avoid index noise
            if np.allclose(vec, 0.0):
                continue
            vec_lit = _vec_to_literal(vec)
            cur.execute(
                """
                INSERT INTO post_vectors (post_id, content_vector, updated_at)
                VALUES (%s, %s::vector, NOW())
                ON CONFLICT (post_id) DO UPDATE
                SET content_vector = EXCLUDED.content_vector, updated_at = NOW()
                """,
                (int(pid), vec_lit),
            )
            inserted += 1

        # Build weighted interactions list
        cur.execute(
            f"""
            SELECT user_id, post_id, weight FROM (
                SELECT user_id, post_id, {REACT_WEIGHT} AS weight FROM reactions
                UNION ALL
                SELECT user_id, post_id, {COMMENT_WEIGHT} FROM comments
                UNION ALL
                SELECT user_id, shared_from_post_id AS post_id, {SHARE_WEIGHT}
                  FROM posts WHERE shared_from_post_id IS NOT NULL
                UNION ALL
                SELECT user_id, post_id, {READ_WEIGHT} FROM post_read_users
            ) t
            WHERE post_id IS NOT NULL
            """
        )
        inter_rows = cur.fetchall()  # list of dicts

        # Load post_vectors from DB and parse
        cur.execute("SELECT post_id, content_vector FROM post_vectors")
        pv_rows = cur.fetchall()
        pv_map = {}
        for r in pv_rows:
            pid = int(r["post_id"])
            vec = parse_db_vector(r["content_vector"])
            if vec is not None and vec.size == TARGET_DIM:
                pv_map[pid] = vec

        user_sum = defaultdict(lambda: np.zeros(TARGET_DIM, dtype=float))
        user_w = defaultdict(float)

        for r in inter_rows:
            uid = int(r["user_id"])
            pid = int(r["post_id"])
            w = float(r["weight"])
            vec = pv_map.get(pid)
            if vec is None:
                continue
            user_sum[uid] += w * vec
            user_w[uid] += w

        profile_count = 0
        # Also create zero-profiles for active users with no weights (optional)
        cur.execute(
            """
            SELECT DISTINCT user_id FROM (
                SELECT user_id FROM reactions
                UNION
                SELECT user_id FROM comments
                UNION
                SELECT user_id FROM post_read_users
                UNION
                SELECT user_id FROM posts
            ) t
            """
        )
        active_users = [int(r["user_id"]) for r in cur.fetchall()]

        for uid in active_users:
            total_w = user_w.get(uid, 0.0)
            if total_w > 0:
                profile_vec = (user_sum[uid] / total_w).tolist()
            else:
                profile_vec = np.zeros(TARGET_DIM, dtype=float).tolist()
            vec_lit = _vec_to_literal(profile_vec)
            cur.execute(
                """
                INSERT INTO user_profiles (user_id, profile_vector, updated_at)
                VALUES (%s, %s::vector, NOW())
                ON CONFLICT (user_id) DO UPDATE
                SET profile_vector = EXCLUDED.profile_vector, updated_at = NOW()
                """,
                (uid, vec_lit),
            )
            profile_count += 1

        conn.commit()
        print(f"[INFO] Refit completed: {inserted}/{len(post_ids)} post_vectors, {profile_count} user_profiles")
    except Exception as e:
        conn.rollback()
        print(f"[ERROR] Refit failed: {e}")
    finally:
        cur.close()
        conn.close()

    recommend_cache.clear()


# ---------------------------
# incremental helpers
# ---------------------------
def incremental_add_post_vector(post_id: int, content_text: str):
    if not content_text or not content_text.strip():
        return
    try:
        vec_sparse = vectorizer.transform([content_text])
        vec = vec_sparse.toarray()[0]
        if vec.size < TARGET_DIM:
            vec = np.pad(vec, (0, TARGET_DIM - vec.size), 'constant')
        vec_lit = _vec_to_literal(vec.tolist())
    except Exception as e:
        print(f"[ERROR] incremental vectorize failed: {e}")
        return

    conn = get_conn()
    cur = conn.cursor()
    try:
        cur.execute(
            """
            INSERT INTO post_vectors (post_id, content_vector, updated_at)
            VALUES (%s, %s::vector, NOW())
            ON CONFLICT (post_id) DO UPDATE
            SET content_vector = EXCLUDED.content_vector, updated_at = NOW()
            """,
            (int(post_id), vec_lit),
        )
        conn.commit()
        print(f"[INFO] incremental_add_post_vector wrote post_id={post_id}")
    except Exception as e:
        conn.rollback()
        print(f"[ERROR] incremental_add_post_vector DB failed: {e}")
    finally:
        cur.close()
        conn.close()


def incremental_update_profile_with_weight(user_id: int, post_id: int, weight: float):
    conn = get_conn()
    cur = conn.cursor()
    try:
        # get post vector
        cur.execute("SELECT content_vector FROM post_vectors WHERE post_id = %s", (int(post_id),))
        prow = cur.fetchone()
        post_vec = parse_db_vector(prow["content_vector"]) if prow else None
        if post_vec is None:
            return

        # get user profile
        cur.execute("SELECT profile_vector FROM user_profiles WHERE user_id = %s", (int(user_id),))
        urow = cur.fetchone()
        if not urow or urow["profile_vector"] is None:
            new_profile = post_vec.tolist()
        else:
            old = parse_db_vector(urow["profile_vector"])
            if old is None:
                new_profile = post_vec.tolist()
            else:
                alpha = ALPHA
                beta = (1.0 - ALPHA)
                numerator = alpha * old + beta * weight * post_vec
                denom = alpha + beta * weight
                if denom == 0:
                    new_profile = old.tolist()
                else:
                    new_profile = (numerator / denom).tolist()

        vec_lit = _vec_to_literal(new_profile)
        cur.execute(
            """
            INSERT INTO user_profiles (user_id, profile_vector, updated_at)
            VALUES (%s, %s::vector, NOW())
            ON CONFLICT (user_id) DO UPDATE
            SET profile_vector = EXCLUDED.profile_vector, updated_at = NOW()
            """,
            (int(user_id), vec_lit),
        )
        conn.commit()
    except Exception as e:
        conn.rollback()
        print(f"[ERROR] incremental_update_profile_with_weight failed: {e}")
    finally:
        cur.close()
        conn.close()


def insert_read_mark(user_id: int, post_id: int):
    conn = get_conn()
    cur = conn.cursor()
    try:
        cur.execute(
            """
            INSERT INTO post_read_users (user_id, post_id, read_at)
            VALUES (%s, %s, NOW())
            ON CONFLICT (user_id, post_id) DO NOTHING
            """,
            (int(user_id), int(post_id)),
        )
        conn.commit()
    except Exception as e:
        conn.rollback()
        print(f"[ERROR] insert_read_mark failed: {e}")
    finally:
        cur.close()
        conn.close()


# ---------------------------
# background refit thread
# ---------------------------
def periodic_refit():
    while True:
        time.sleep(REFIT_INTERVAL_SECONDS)
        try:
            refit_vectors()
        except Exception as e:
            print(f"[ERROR] periodic_refit: {e}")


threading.Thread(target=periodic_refit, daemon=True).start()


# ---------------------------
# API models & routes
# ---------------------------
class InteractionRequest(BaseModel):
    user_id: int
    post_id: int


class NewPostRequest(BaseModel):
    post_id: int
    content_text: str


@app.get("/recommend")
def recommend(user_id: int, offset: int = Query(0, ge=0), limit: int = Query(TOP_N, ge=1)):
    cache_key = (user_id, offset, limit)
    if cache_key in recommend_cache:
        return recommend_cache[cache_key]

    conn = get_conn()
    cur = conn.cursor()
    try:
        cur.execute("SELECT profile_vector FROM user_profiles WHERE user_id = %s", (int(user_id),))
        urow = cur.fetchone()
        user_vec = None
        if urow and urow.get("profile_vector") is not None:
            user_vec = parse_db_vector(urow["profile_vector"])

        # if no profile or profile is all zeros -> chronological cold-start (exclude reacted/read)
        is_zero_profile = True
        if user_vec is not None:
            try:
                is_zero_profile = np.allclose(user_vec.astype(float), 0.0)
            except Exception:
                # fallback: treat as non-zero only if parse succeeded
                is_zero_profile = False

        # count unread
        cur.execute(
            """
            SELECT COUNT(*) AS cnt
            FROM post_vectors pv
            JOIN posts p ON pv.post_id = p.id
            WHERE p.visibility = TRUE
              AND pv.post_id NOT IN (SELECT post_id FROM reactions WHERE user_id = %s)
              AND pv.post_id NOT IN (SELECT post_id FROM post_read_users WHERE user_id = %s)
            """,
            (int(user_id), int(user_id)),
        )
        num_unread = int(cur.fetchone()["cnt"] or 0)

        remaining = limit
        cur_offset = offset
        results = []

        # build base select fragments
        if is_zero_profile:
            # score is 0 in cold-start mode
            base_select = """
                SELECT p.id,
                       COALESCE(p.content_text, '') || COALESCE(' ' || sp.content_text, '') AS content_text,
                       0.0 AS score
            """
        else:
            user_vec_lit = _vec_to_literal(user_vec.tolist())
            base_select = """
                SELECT p.id,
                       COALESCE(p.content_text, '') || COALESCE(' ' || sp.content_text, '') AS content_text,
                       (1 - (pv.content_vector <=> %s::vector)) AS score
            """

        from_part = """
            FROM post_vectors pv
            JOIN posts p ON pv.post_id = p.id
            LEFT JOIN posts sp ON p.shared_from_post_id = sp.id
            WHERE p.visibility = TRUE
        """

        # unread block
        if cur_offset < num_unread:
            take = min(remaining, num_unread - cur_offset)
            unread_query = base_select + from_part + """
                AND p.id NOT IN (SELECT post_id FROM reactions WHERE user_id = %s)
                AND p.id NOT IN (SELECT post_id FROM post_read_users WHERE user_id = %s)
            """
            if is_zero_profile:
                unread_query += " ORDER BY p.created_at DESC OFFSET %s LIMIT %s"
                cur.execute(unread_query, (int(user_id), int(user_id), cur_offset, take))
            else:
                unread_query += " ORDER BY pv.content_vector <=> %s::vector OFFSET %s LIMIT %s"
                cur.execute(unread_query, (int(user_user := 0),))  # dummy to satisfy param order below (we'll re-run properly)
                # actually execute with correct params:
                cur.execute(unread_query, (user_vec_lit, int(user_id), int(user_id), user_vec_lit, cur_offset, take))
            rows = cur.fetchall()
            results.extend([{"id": r["id"], "content_text": r["content_text"], "score": float(r["score"])} for r in rows])
            remaining -= len(rows)
            cur_offset = 0
        else:
            cur_offset -= num_unread

        # read block
        if remaining > 0:
            read_query = base_select + from_part + """
                AND p.id NOT IN (SELECT post_id FROM reactions WHERE user_id = %s)
                AND p.id IN (SELECT post_id FROM post_read_users WHERE user_id = %s)
            """
            if is_zero_profile:
                read_query += " ORDER BY p.created_at DESC OFFSET %s LIMIT %s"
                cur.execute(read_query, (int(user_id), int(user_id), cur_offset, remaining))
            else:
                read_query += " ORDER BY pv.content_vector <=> %s::vector OFFSET %s LIMIT %s"
                cur.execute(read_query, (user_vec_lit, int(user_id), int(user_id), user_vec_lit, cur_offset, remaining))
            rows = cur.fetchall()
            results.extend([{"id": r["id"], "content_text": r["content_text"], "score": float(r["score"])} for r in rows])

        recommend_cache[cache_key] = results
        return results
    except Exception as e:
        # log and return empty
        print(f"[ERROR] recommend failed: {e}")
        return []
    finally:
        cur.close()
        conn.close()


def clear_user_cache(user_id: int):
    keys = [k for k in list(recommend_cache.keys()) if k[0] == user_id]
    for k in keys:
        del recommend_cache[k]


@app.post("/react")
def user_reacts(req: InteractionRequest):
    incremental_update_profile_with_weight(req.user_id, req.post_id, REACT_WEIGHT)
    clear_user_cache(req.user_id)
    return {"status": "ok"}


@app.post("/comment")
def user_comments(req: InteractionRequest):
    incremental_update_profile_with_weight(req.user_id, req.post_id, COMMENT_WEIGHT)
    clear_user_cache(req.user_id)
    return {"status": "ok"}


@app.post("/share")
def user_shares(req: InteractionRequest):
    incremental_update_profile_with_weight(req.user_id, req.post_id, SHARE_WEIGHT)
    clear_user_cache(req.user_id)
    return {"status": "ok"}


@app.post("/read")
def user_reads(req: InteractionRequest):
    insert_read_mark(req.user_id, req.post_id)
    incremental_update_profile_with_weight(req.user_id, req.post_id, READ_WEIGHT)
    clear_user_cache(req.user_id)
    return {"status": "ok"}


@app.post("/new_post")
def add_post(req: NewPostRequest):
    incremental_add_post_vector(req.post_id, req.content_text)
    recommend_cache.clear()
    return {"status": "ok"}


@app.get("/health")
def health():
    try:
        conn = get_conn()
        conn.close()
        return {"status": "ok", "db": "connected"}
    except Exception as e:
        return {"status": "error", "detail": str(e)}


# ---------------------------
# INIT
# ---------------------------
print("[INFO] Starting recommendation service...")
refit_vectors()
print("[INFO] Recommendation server ready!")
