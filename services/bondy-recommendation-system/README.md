# Recommendation Server (FastAPI)

A lightweight content recommendation microservice built with **FastAPI**. It generates personalized post recommendations for users based on their past reactions using **TF-IDF** vectorization and **cosine similarity**.

---

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Data Model](#data-model)
- [How It Works](#how-it-works)
- [Installation](#installation)
- [Configuration](#configuration)
- [Running the Server](#running-the-server)
- [API Endpoints](#api-endpoints)
- [Incremental Updates](#incremental-updates)
- [Periodic Refit](#periodic-refit)
- [Cold Start Strategy](#cold-start-strategy)
- [Example Usage](#example-usage)
- [Performance Notes](#performance-notes)
- [Security Considerations](#security-considerations)
- [Troubleshooting](#troubleshooting)
- [Future Improvements](#future-improvements)
- [License](#license)

---

## Overview

This service recommends posts that a user has **not reacted to yet**, ranking them by similarity between:

- The user’s profile vector (mean TF-IDF of reacted posts)
- Each candidate post’s TF-IDF vector

It supports:

- Paginated recommendation (`offset`, `limit`)
- Incremental updates when users react or new posts are created
- Periodic full refitting (reload DB + rebuild TF-IDF + rebuild user profiles)

---

## Architecture

```
PostgreSQL
   ├─ posts(id, content_text, ...)
   └─ reactions(user_id, post_id)

FastAPI Server
   ├─ Load posts & reactions on startup
   ├─ Build TF-IDF matrix for post content
   ├─ Build user_profiles[user_id] = mean(post_vectors reacted)
   ├─ /recommend: filter out reacted posts, compute cosine similarity, sort & paginate
   ├─ /react: incremental profile update (alpha blend)
   └─ /new_post: add post + refit TF-IDF
```

---

## Features

- Load posts & reactions from PostgreSQL
- TF-IDF vectorization with configurable max features
- User profile construction (mean of reacted post vectors)
- Cosine similarity scoring for recommendations
- Exclusion of already reacted posts
- Incremental profile update on `/react`
- Incremental post additions on `/new_post`
- Periodic background refit (configurable interval)
- Pagination via `offset` and `limit`
- Cold-start fallback for users without interactions

---

## Tech Stack

| Component     | Technology          |
| ------------- | ------------------- |
| Language      | Python 3.9+         |
| Framework     | FastAPI             |
| Data Access   | psycopg2 + pandas   |
| Vectorization | scikit-learn TF-IDF |
| Similarity    | cosine similarity   |
| Server        | uvicorn             |
| Data Store    | PostgreSQL          |

---

## Data Model

### Table: `posts`

| Column       | Type   | Description         |
| ------------ | ------ | ------------------- |
| id           | bigint | Primary key         |
| content_text | text   | Raw textual content |

### Table: `reactions`

| Column  | Type   | Description              |
| ------- | ------ | ------------------------ |
| user_id | bigint | User who reacted         |
| post_id | bigint | Post that was reacted to |

User profile = average vector of all reacted posts.

---

## How It Works

1. On startup:
   - Load all posts and reactions
   - Fit TF-IDF over `content_text`
   - Build `user_profiles` (one vector per user)
2. On `/recommend`:
   - If user has no profile → return fallback (newest posts)
   - Else compute similarity for all unreacted posts
   - Sort by descending similarity score
   - Apply `offset` + `limit`
3. On `/react`:
   - Blend new post vector into existing user vector (`alpha = 0.8`)
4. On `/new_post`:
   - Append post and refit TF-IDF (rebuild matrix)
5. Periodically:
   - Reload DB + refit TF-IDF + rebuild profiles (full consistency)

---

## Installation

```bash
git clone <your-repo-url>
cd recommendation-server
python -m venv .venv
source .venv/bin/activate  # Windows: .venv\Scripts\activate
pip install --upgrade pip
pip install fastapi uvicorn pandas numpy scikit-learn psycopg2-binary pydantic
```

---

## Configuration

Edit the **DB_CONFIG** section in the Python file (`recommendation_server.py` or `server.py`):

```python
DB_CONFIG = {
    "host": "localhost",
    "port": 5433,
    "dbname": "bondy-interaction",
    "user": "postgres",
    "password": "123456",
}
```

Other parameters:

```python
TOP_N = 10                 # Default recommendation limit
REFIT_INTERVAL_SECONDS = 10  # Frequency of full refit (e.g., 10s or 3600 for hourly)
```

You can parameterize these using environment variables instead (future improvement).

---

## Running the Server

```bash
uvicorn recommendation_server:app --reload --port 8000
```

Server accessible at: `http://127.0.0.1:8000`

Interactive docs:

- Swagger UI: `http://127.0.0.1:8000/docs`
- ReDoc: `http://127.0.0.1:8000/redoc`

---

## API Endpoints

### 1. GET `/recommend`

Returns ranked, paginated recommended posts.

Query Parameters:
| Name | Type | Required | Default | Description |
|----------|------|----------|---------|-----------------------------------|
| user_id | int | yes | - | Target user |
| offset | int | no | 0 | Pagination start index |
| limit | int | no | 10 | Number of items to return |

Response (list):

```json
[
  { "id": 69, "content_text": "Đi biển mùa nào đẹp nhất?", "score": 0.2307 },
  ...
]
```

### 2. POST `/react`

Incrementally update a user’s profile after reacting.

Body:

```json
{
  "user_id": 4,
  "post_id": 69
}
```

Response:

```json
{ "status": "ok" }
```

### 3. POST `/new_post`

Add a new post and refit TF-IDF.

Body:

```json
{
  "post_id": 105,
  "content_text": "Du lịch mùa đông ở Sa Pa"
}
```

Response:

```json
{ "status": "ok" }
```

---

## Incremental Updates

- `/react` triggers an in-memory blend:
  ```
  new_profile = alpha * old_profile + (1 - alpha) * post_vector
  ```
- `/new_post` refits the TF-IDF model (current simple approach).

---

## Periodic Refit

Background daemon thread:

- Sleeps `REFIT_INTERVAL_SECONDS`
- Reloads DB
- Fits TF-IDF from scratch
- Rebuilds all `user_profiles`

Adjust interval based on system size & acceptable staleness.

---

## Cold Start Strategy

If:

- User not in `user_profiles` OR
- User vector is all zeros

Then:

- Return newest posts (or optionally trending/random)
- With `score = 0.0`

This prevents empty recommendation lists.

---

## Example Usage

```bash
# Recommend for user 4, first page
curl -s "http://127.0.0.1:8000/recommend?user_id=4&offset=0&limit=5" | python -m json.tool

# Next page
curl -s "http://127.0.0.1:8000/recommend?user_id=4&offset=5&limit=5" | python -m json.tool

# User reacts
curl -X POST -H "Content-Type: application/json" \
     -d '{"user_id":4,"post_id":69}' \
     http://127.0.0.1:8000/react

# Add new post
curl -X POST -H "Content-Type: application/json" \
     -d '{"post_id":120,"content_text":"Ẩm thực đường phố Hà Nội"}' \
     http://127.0.0.1:8000/new_post
```

---

## Performance Notes

| Aspect           | Current Approach          | Improvement Ideas                            |
| ---------------- | ------------------------- | -------------------------------------------- |
| TF-IDF rebuild   | Full refit on new post    | Use partial vocabulary / incremental fitting |
| User profiles    | Mean of reacted posts     | Weighted by recency or reaction strength     |
| Similarity       | Cosine over dense vectors | Switch to sparse operations / ANN            |
| Scaling posts    | In-memory pandas          | Migrate to vector store (Faiss, Milvus)      |
| Cold start       | Newest posts fallback     | Hybrid popularity + embedding similarity     |
| Reactions impact | Simple alpha blend        | Time decay / per-category profile            |

---

## Security Considerations

- No authentication layer yet → Add API key or JWT if exposed publicly.
- Validate `post_id` and `user_id` against DB existence (currently implicit).
- Prevent abuse (rate limit `/recommend`).
- Sanitize content ingestion if external sources are introduced.

---

## Troubleshooting

| Issue                                | Cause                            | Resolution                                            |
| ------------------------------------ | -------------------------------- | ----------------------------------------------------- |
| Always same recommendations          | User has zero-vector profile     | Trigger reactions or adjust cold-start logic          |
| Scores do not change after reaction  | Periodic refit interval too long | Lower `REFIT_INTERVAL_SECONDS` or rebuild on `/react` |
| Slow startup                         | Large DB tables                  | Add indexing & load only needed columns               |
| Memory usage grows                   | Continuous refits + large TF-IDF | Limit `max_features`, add pruning strategies          |
| Offset pagination returns duplicates | Not enough unreacted posts       | Check dataset size / reaction exclusion logic         |

---

## Future Improvements

- Add popularity blending (e.g., score = 0.7 _ similarity + 0.3 _ popularity)
- Multilingual handling (language detection + separate models)
- Category-aware user profiles
- Replace TF-IDF with transformer embeddings (Sentence-BERT)
- Streaming updates via Kafka
- Deploy with Docker & health/readiness endpoints
- Add `/metrics` (Prometheus) for monitoring
- Config via environment variables instead of hard-coded constants
- Unit tests for recommend logic & cold-start fallback

---

## License

This project can be distributed under your organization’s internal license.  
Add an open-source license (e.g., MIT) if intended for public release.

---

## Quick Start (Copy/Paste)

```bash
pip install fastapi uvicorn pandas numpy scikit-learn psycopg2-binary pydantic
uvicorn server:app --reload --port 8000
```

Then test:

```bash
curl "http://127.0.0.1:8000/recommend?user_id=1&offset=0&limit=5"
```

---

Feel free to adjust this README to match deployment context (Docker, Kubernetes, CI/CD, etc.). Let me know if you’d like a Dockerfile or Helm chart template.
