-- Run against an existing QLPT database. Safe to repeat; preserves existing tokens.
BEGIN;
ALTER TABLE users ALTER COLUMN fcm_token TYPE text;
COMMIT;
