ALTER TABLE recipe
    ADD COLUMN owner_id BIGINT REFERENCES user_account(id),
    ADD COLUMN created TIMESTAMP DEFAULT CURRENT_TIMESTAMP;