ALTER TABLE recipe_list
    ADD COLUMN owner_id BIGINT REFERENCES user_account(id);

ALTER TABLE recipe_list
    ALTER COLUMN created TYPE TIMESTAMP;