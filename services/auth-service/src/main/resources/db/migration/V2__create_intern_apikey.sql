INSERT INTO api_keys (name, key_hash, prefix, expires_at, active, created_at)
VALUES (
           'internal-key',
           '213886978f99ddad7fc449d9031fed6013eda1231aa9a016471879365aa91dbb',
           'internal',
           NULL,
           TRUE,
           NOW()
       );
