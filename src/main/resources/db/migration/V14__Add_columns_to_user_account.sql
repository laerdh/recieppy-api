ALTER TABLE user_account
    DROP COLUMN firebase_id,
    DROP COLUMN token;

ALTER TABLE user_account
    ADD COLUMN external_id VARCHAR UNIQUE;