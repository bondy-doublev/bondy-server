INSERT INTO api_keys (name, key_hash, prefix, expires_at, active, created_at)
VALUES (
           'internal-key',
           '36f053f2f4f4cc612155ad84999c93c308ed73592b9d53701c9d586cf6cb1b98',
           'internal',
           NULL,
           TRUE,
           NOW()
       );
