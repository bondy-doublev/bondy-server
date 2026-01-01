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

load_dotenv()

app = FastAPI()

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
REFIT_INTERVAL_SECONDS = int(os.getenv("REFIT_INTERVAL_SECONDS", 10 * 60))

# ---------------------------
# GLOBAL DATA
# ---------------------------
posts = pd.DataFrame()
reactions = pd.DataFrame()
post_vectors = None
vectorizer = TfidfVectorizer(max_features=500)
user_profiles: dict[int, np.ndarray] = {}


# ---------------------------
# HELPER FUNCTIONS
# ---------------------------
def load_data_from_db():
  """
  Load posts + reactions từ DB.
  ⚠️ ĐÃ SỬA:
  - Lấy thêm shared_from_post_id
  - Gộp content_text của post + content_text của post gốc (nếu có)
  """
  global posts, reactions

  conn = psycopg2.connect(**DB_CONFIG)

  # Ở đây giả định bảng posts đã có cột shared_from_post_id (FK đến posts.id)
  posts = pd.read_sql(
      """
      SELECT
          p.id,
          -- gộp content của post + content của post gốc (nếu có)
          COALESCE(p.content_text, '') ||
          COALESCE(' ' || sp.content_text, '') AS content_text
      FROM posts p
      LEFT JOIN posts sp ON p.shared_from_post_id = sp.id
      ORDER BY p.id
      """,
      conn,
  )

  reactions = pd.read_sql(
      """
      SELECT user_id, post_id
      FROM reactions
      ORDER BY user_id
      """,
      conn,
  )

  conn.close()
  print("[INFO] Loaded posts & reactions from DB")


def fit_tfidf():
  global post_vectors, vectorizer
  if posts.empty:
    post_vectors = None
    print("[INFO] No posts to fit TF-IDF")
    return

  post_vectors = vectorizer.fit_transform(posts["content_text"].fillna(""))
  print("[INFO] TF-IDF fitted. Shape:", post_vectors.shape)


def build_user_profiles():
  global user_profiles, posts, reactions, post_vectors
  user_profiles = {}

  if post_vectors is None or posts.empty:
    print("[INFO] Skip building user profiles: no posts/post_vectors")
    return

  user_ids = reactions["user_id"].unique()

  for uid in user_ids:
    reacted_post_ids = reactions[reactions["user_id"] == uid]["post_id"].values
    reacted_indices = posts[posts["id"].isin(reacted_post_ids)].index

    if len(reacted_indices) == 0:
      user_profiles[uid] = np.zeros(post_vectors.shape[1])
    else:
      user_vector = post_vectors[reacted_indices].mean(axis=0)
      user_profiles[uid] = np.asarray(user_vector).flatten()

  print("[INFO] User profiles rebuilt. Total users:", len(user_profiles))


def incremental_update_user(user_id: int, post_id: int):
  """
  Cập nhật profile user khi react thêm 1 post.
  Không đụng đến DB, dùng posts/post_vectors hiện tại trong RAM.
  """
  global user_profiles, posts, post_vectors

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


def incremental_add_post(post_id: int, content_text: str):
  """
  Thêm post mới vào RAM.
  Ở đây em keep simple: refit TF-IDF và rebuild user_profiles,
  vì tạo post không nhiều bằng react.
  """
  global posts, post_vectors, vectorizer

  # Thêm dòng mới (tạm dùng chỉ content_text local; khi refit định kỳ sẽ
  # lấy đúng content gộp từ DB – cả shared_from nếu có)
  posts = pd.concat(
      [posts, pd.DataFrame([{"id": post_id, "content_text": content_text}])],
      ignore_index=True,
  )

  if not posts.empty:
    post_vectors = vectorizer.fit_transform(posts["content_text"].fillna(""))
    print("[INFO] TF-IDF refit after new post. Shape:", post_vectors.shape)
  else:
    post_vectors = None

  # Để cho chắc, rebuild lại user_profiles để dimension khớp
  build_user_profiles()


# ---------------------------
# BACKGROUND REFIT THREAD
# ---------------------------
def periodic_refit():
  while True:
    time.sleep(REFIT_INTERVAL_SECONDS)
    print("[INFO] Periodic refit: reloading data & rebuilding...")
    load_data_from_db()
    fit_tfidf()
    build_user_profiles()
    print("[INFO] Refit done.")


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
  global posts, post_vectors, user_profiles, reactions

  if posts.empty or post_vectors is None:
    return []

  # Cold-start: user chưa có profile => trả newest/random
  if user_id not in user_profiles:
    cold = (
        posts.sort_values(by="id", ascending=False)
        .reset_index(drop=True)
        .iloc[offset : offset + limit]
    )
    cold = cold.copy()
    cold["score"] = 0.0
    return cold[["id", "content_text", "score"]].to_dict(orient="records")

  user_vec = user_profiles[user_id]
  if np.allclose(user_vec, 0):
    cold = (
        posts.sort_values(by("id"), ascending=False)
        .reset_index(drop=True)
        .iloc[offset : offset + limit]
    )
    cold = cold.copy()
    cold["score"] = 0.0
    return cold[["id", "content_text", "score"]].to_dict(orient="records")

  # Tính similarity
  similarities = cosine_similarity(post_vectors, user_vec.reshape(1, -1)).flatten()

  # Nếu vì lý do nào đó length lệch (posts vừa reload mà vector chưa)
  if len(similarities) != len(posts):
    # hard reset cho chắc
    print(
        f"[WARN] Length mismatch: sims={len(similarities)}, posts={len(posts)}. "
        "Reloading & rebuilding..."
    )
    load_data_from_db()
    fit_tfidf()
    build_user_profiles()

    if posts.empty or post_vectors is None or user_id not in user_profiles:
      return []

    user_vec = user_profiles[user_id]
    similarities = cosine_similarity(
        post_vectors, user_vec.reshape(1, -1)
    ).flatten()

  # Gán score vào một bản copy để tránh side-effect giữa các request
  posts_with_score = posts.copy()
  posts_with_score["score"] = similarities

  reacted_ids = reactions[reactions["user_id"] == user_id]["post_id"].values
  recs = posts_with_score[~posts_with_score["id"].isin(reacted_ids)]

  print(f"Total unreacted posts: {len(recs)}")
  print(f"Offset: {offset}, Limit: {limit}")

  recs_sorted = recs.sort_values(by="score", ascending=False).reset_index(drop=True)
  recs_paginated = recs_sorted.iloc[offset : offset + limit]

  print(f"Paginated shape: {recs_paginated.shape}")

  top_posts = recs_paginated[["id", "content_text", "score"]]
  return top_posts.to_dict(orient="records")


@app.post("/react")
def user_reacts(req: ReactRequest):
  """
  ⚠️ ĐÃ SỬA: bỏ load_data_from_db() để tránh lệch size giữa posts và post_vectors.
  Chỉ update profile trong RAM, phần reload đã có thread periodic_refit lo.
  """
  incremental_update_user(req.user_id, req.post_id)
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
load_data_from_db()
fit_tfidf()
build_user_profiles()

print("[INFO] Recommendation server ready.")
