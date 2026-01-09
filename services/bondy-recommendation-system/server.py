import os
from dotenv import load_dotenv
from fastapi import FastAPI, Query
from pydantic import BaseModel
import pandas as pd
import numpy as np
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity
import threading
import time
import psycopg2
from cachetools import TTLCache

load_dotenv()

app = FastAPI(title="Bondy Recommendation Service")

# ---------------------------
# CONFIG
# ---------------------------
DB_CONFIG = {
    "host": os.getenv("DB_HOST"),
    "port": int(os.getenv("DB_PORT")),
    "dbname": os.getenv("DB_NAME"),
    "user": os.getenv("DB_USER"),
    "password": os.getenv("DB_PASS"),
}

TOP_N = int(os.getenv("TOP_N", 10))
REFIT_INTERVAL_SECONDS = int(os.getenv("REFIT_INTERVAL_SECONDS", 600))

# Cache recommend: 10k keys, TTL 5 phút
recommend_cache = TTLCache(maxsize=10000, ttl=300)

# ---------------------------
# GLOBAL DATA
# ---------------------------
posts = pd.DataFrame(columns=["id", "content_text"])
reactions = pd.DataFrame(columns=["user_id", "post_id"])
post_vectors = None
vectorizer = TfidfVectorizer(max_features=500, stop_words="english")
user_profiles: dict[int, np.ndarray] = {}
user_reacted_sets: dict[int, set] = {}  # Cache reacted post IDs per user


# ---------------------------
# HELPER FUNCTIONS
# ---------------------------
def load_data_from_db():
    global posts, reactions
    conn = psycopg2.connect(**DB_CONFIG)
    try:
        posts = pd.read_sql(
            """
            SELECT
                p.id,
                COALESCE(p.content_text, '') ||
                COALESCE(' ' || sp.content_text, '') AS content_text
            FROM posts p
            LEFT JOIN posts sp ON p.shared_from_post_id = sp.id
            ORDER BY p.id
            """,
            conn,
        )

        reactions = pd.read_sql(
            "SELECT user_id, post_id FROM reactions ORDER BY user_id",
            conn,
        )
        print(f"[INFO] Loaded {len(posts)} posts & {len(reactions)} reactions from DB")
    finally:
        conn.close()


def fit_tfidf():
    global post_vectors
    if posts.empty:
        post_vectors = None
        return
    post_vectors = vectorizer.fit_transform(posts["content_text"].fillna(""))
    print(f"[INFO] TF-IDF fitted. Shape: {post_vectors.shape}")


def build_user_profiles():
    global user_profiles, user_reacted_sets
    user_profiles.clear()
    user_reacted_sets.clear()

    if post_vectors is None or reactions.empty:
        print("[INFO] Skipped building user profiles: no data")
        return

    # Mapping post_id → index
    post_id_to_idx = dict(zip(posts["id"], posts.index))

    # Group reactions
    grouped = reactions.groupby("user_id")["post_id"].apply(list)

    for user_id, post_ids in grouped.items():
        valid_indices = [post_id_to_idx.get(pid) for pid in post_ids if pid in post_id_to_idx]
        if not valid_indices:
            user_profiles[user_id] = np.zeros(post_vectors.shape[1])
        else:
            user_vector = post_vectors[valid_indices].mean(axis=0)
            user_profiles[user_id] = np.asarray(user_vector).flatten()

        # Cache reacted set
        user_reacted_sets[user_id] = set(post_ids)

    print(f"[INFO] User profiles rebuilt: {len(user_profiles)} users")


def incremental_update_user(user_id: int, post_id: int):
    global user_profiles, user_reacted_sets
    if post_vectors is None or posts.empty:
        return

    post_idx = posts[posts["id"] == post_id].index
    if len(post_idx) == 0:
        return

    post_vec = np.asarray(post_vectors[post_idx].mean(axis=0)).flatten()

    if user_id not in user_profiles:
        user_profiles[user_id] = post_vec
    else:
        alpha = 0.8
        user_profiles[user_id] = alpha * user_profiles[user_id] + (1 - alpha) * post_vec

    # Update reacted set
    if user_id not in user_reacted_sets:
        user_reacted_sets[user_id] = set()
    user_reacted_sets[user_id].add(post_id)


def incremental_add_post(post_id: int, content_text: str):
    """
    Chỉ thêm post vào RAM, KHÔNG refit ngay.
    Post mới sẽ được vector hóa chính xác trong lần refit định kỳ tiếp theo.
    """
    global posts
    new_row = pd.DataFrame([{"id": post_id, "content_text": content_text}])
    posts = pd.concat([posts, new_row], ignore_index=True)
    print(f"[INFO] Added new post {post_id} to memory (vectorized in next refit)")


# ---------------------------
# BACKGROUND REFIT THREAD
# ---------------------------
def periodic_refit():
    while True:
        time.sleep(REFIT_INTERVAL_SECONDS)
        print("[INFO] Starting periodic refit...")
        load_data_from_db()
        fit_tfidf()
        build_user_profiles()
        recommend_cache.clear()  # Xóa cache cũ sau khi dữ liệu thay đổi
        print("[INFO] Periodic refit completed.")


threading.Thread(target=periodic_refit, daemon=True).start()


# ---------------------------
# API MODELS
# ---------------------------
class ReactRequest(BaseModel):
    user_id: int
    post_id: int


class NewPostRequest(BaseModel):
    post_id: int
    content_text: str


# ---------------------------
# API ROUTES
# ---------------------------
@app.get("/recommend")
def recommend(
    user_id: int,
    offset: int = Query(0, ge=0),
    limit: int = Query(TOP_N, ge=1),
):
    cache_key = (user_id, offset, limit)
    if cache_key in recommend_cache:
        return recommend_cache[cache_key]

    if posts.empty or post_vectors is None:
        return []

    # Cold start
    if user_id not in user_profiles:
        result = posts.sort_values(by="id", ascending=False).iloc[offset:offset + limit]
        result = result.copy()
        result["score"] = 0.0
        result = result[["id", "content_text", "score"]].to_dict(orient="records")
        recommend_cache[cache_key] = result
        return result

    user_vec = user_profiles[user_id]

    # Tính similarity
    similarities = cosine_similarity(post_vectors, user_vec.reshape(1, -1)).flatten()

    # Gán score
    posts_with_score = posts.copy()
    posts_with_score["score"] = similarities

    # Filter đã reacted (dùng set → nhanh hơn)
    reacted_ids = user_reacted_sets.get(user_id, set())
    recs = posts_with_score[~posts_with_score["id"].isin(reacted_ids)]

    if recs.empty:
        result = []
    else:
        recs_sorted = recs.sort_values(by="score", ascending=False).reset_index(drop=True)
        top_slice = recs_sorted.iloc[offset:offset + limit]
        result = top_slice[["id", "content_text", "score"]].to_dict(orient="records")

    recommend_cache[cache_key] = result
    return result


@app.post("/react")
def user_reacts(req: ReactRequest):
    incremental_update_user(req.user_id, req.post_id)
    # Xóa cache liên quan đến user này
    keys_to_clear = [k for k in recommend_cache.keys() if k[0] == req.user_id]
    for k in keys_to_clear:
        del recommend_cache[k]
    return {"status": "ok"}


@app.post("/new_post")
def add_post(req: NewPostRequest):
    incremental_add_post(req.post_id, req.content_text)
    return {"status": "ok"}


@app.get("/health")
def health():
    try:
        conn = psycopg2.connect(**DB_CONFIG)
        conn.close()
        return {"status": "ok", "db": "connected"}
    except Exception as e:
        return {"status": "error", "db": "failed", "detail": str(e)}


# ---------------------------
# INIT LOAD
# ---------------------------
print("[INFO] Starting recommendation service...")
load_data_from_db()
fit_tfidf()
build_user_profiles()
print("[INFO] Recommendation server ready and running!")