CREATE TABLE location_invite(
    id SERIAL PRIMARY KEY,
    sent TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    location_id SERIAL NOT NULL REFERENCES location(id),
    email VARCHAR,
    invite_code VARCHAR(6) NOT NULL,
    accepted_user_id BIGINT REFERENCES user_account(id)
);