import os
from dotenv import load_dotenv
from fastapi import FastAPI
from pydantic import BaseModel
import pandas as pd
import numpy as np
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity
import threading
import time
import psycopg2
from fastapi import Query

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
REFIT_INTERVAL_SECONDS = int(os.getenv("REFIT_INTERVAL_SECONDS"))

# ---------------------------
# GLOBAL DATA
# ---------------------------
posts = pd.DataFrame()
reactions = pd.DataFrame()
post_vectors = None
vectorizer = TfidfVectorizer(max_features=500)
user_profiles = {}


# ---------------------------
# HELPER FUNCTIONS
# ---------------------------
def load_data_from_db():
    global posts, reactions

    conn = psycopg2.connect(**DB_CONFIG)

    posts = pd.read_sql(
        """
        SELECT id, content_text
        FROM posts
        ORDER BY id
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
    post_vectors = vectorizer.fit_transform(posts["content_text"].fillna(""))
    print("[INFO] TF-IDF fitted")


def build_user_profiles():
    global user_profiles, posts, reactions, post_vectors
    user_profiles = {}

    user_ids = reactions["user_id"].unique()

    for uid in user_ids:
        reacted_post_ids = reactions[reactions["user_id"] == uid]["post_id"].values
        reacted_indices = posts[posts["id"].isin(reacted_post_ids)].index

        if len(reacted_indices) == 0:
            user_profiles[uid] = np.zeros(post_vectors.shape[1])
        else:
            user_vector = post_vectors[reacted_indices].mean(axis=0)
            user_profiles[uid] = np.asarray(user_vector).flatten()

    print("[INFO] User profiles rebuilt")


def incremental_update_user(user_id, post_id):
    global user_profiles, posts, post_vectors

    post_idx = posts[posts["id"] == post_id].index
    if len(post_idx) == 0:
        return

    post_vec = np.asarray(post_vectors[post_idx].mean(axis=0)).flatten()

    if user_id not in user_profiles:
        user_profiles[user_id] = post_vec
    else:
        alpha = 0.8
        user_profiles[user_id] = alpha * user_profiles[user_id] + (1 - alpha) * post_vec


def incremental_add_post(post_id, content_text):
    global posts, post_vectors, vectorizer

    posts = pd.concat(
        [posts, pd.DataFrame([{"id": post_id, "content_text": content_text}])],
        ignore_index=True,
    )

    post_vectors = vectorizer.fit_transform(posts["content_text"].fillna(""))


# ---------------------------
# BACKGROUND REFIT THREAD
# ---------------------------
def periodic_refit():
    while True:
        time.sleep(REFIT_INTERVAL_SECONDS)
        print("[INFO] Refit TF-IDF & rebuild profiles...")

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
    user_id: int, offset: int = Query(0, ge=0), limit: int = Query(TOP_N, ge=1)
):
    if user_id not in user_profiles:
        return []

    user_vec = user_profiles[user_id]

    similarities = cosine_similarity(post_vectors, user_vec.reshape(1, -1)).flatten()
    posts["score"] = similarities

    reacted_ids = reactions[reactions["user_id"] == user_id]["post_id"].values
    recs = posts[~posts["id"].isin(reacted_ids)]
    print(
        f"Total unreacted posts: {len(recs)}"
    )  # <= DEBUG: Số post recommend ============
    print(f"Offset: {offset}, Limit: {limit}")

    recs_sorted = recs.sort_values(by="score", ascending=False).reset_index(drop=True)
    recs_paginated = recs_sorted.iloc[offset : offset + limit]

    print(f"Paginated shape: {recs_paginated.shape}")  # <= DEBUG: Xem số phần tử trả về

    top_posts = recs_paginated[["id", "content_text", "score"]]
    return top_posts.to_dict(orient="records")


@app.post("/react")
def user_reacts(req: ReactRequest):
    incremental_update_user(req.user_id, req.post_id)
    return {"status": "ok"}


@app.post("/new_post")
def add_post(req: NewPostRequest):
    incremental_add_post(req.post_id, req.content_text)
    return {"status": "ok"}


# ---------------------------
# INIT LOAD
# ---------------------------
load_data_from_db()
fit_tfidf()
build_user_profiles()

print("[INFO] Recommendation server ready.")
