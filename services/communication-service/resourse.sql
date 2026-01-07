create extension if not exists vector;

create table faqs (
  id uuid primary key default gen_random_uuid(),
  question text,
  answer text,
  embedding vector(1536)
);

create index on faqs
using ivfflat
(embedding vector_cosine_ops)
with
(lists = 100);

create or replace function match_faqs
(
  query_embedding vector
(1536),
  match_count int
)
returns table
(
  id uuid,
  question text,
  answer text,
  similarity float
)
language sql stable
as $$
select
  id,
  question,
  answer,
  1 - (embedding <=> query_embedding) as similarity
from faqs
order by embedding
<=> query_embedding
  limit match_count;
$$;