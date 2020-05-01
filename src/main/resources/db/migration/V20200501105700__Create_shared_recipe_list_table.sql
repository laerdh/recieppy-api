CREATE TABLE shared_recipe_list(
    id SERIAL PRIMARY KEY,
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    recipe_list_id BIGINT NOT NULL REFERENCES recipe_list(id),
    sharer_id BIGINT NOT NULL REFERENCES user_account(id),
    recipient_id BIGINT REFERENCES user_account(id),
    invite_code VARCHAR(6),
    accepted BOOLEAN DEFAULT false
);