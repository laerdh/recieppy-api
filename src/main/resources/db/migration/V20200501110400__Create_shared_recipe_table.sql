CREATE TABLE shared_recipe(
    id SERIAL PRIMARY KEY,
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    recipe_id BIGINT NOT NULL REFERENCES recipe(id),
    sharer_id BIGINT NOT NULL REFERENCES user_account(id),
    recipient_id BIGINT REFERENCES user_account(id),
    invite_code VARCHAR(6),
    accepted BOOLEAN
);