CREATE TABLE location_user_account (
  location_id SERIAL NOT NULL REFERENCES location(id),
  user_account_id SERIAL NOT NULL REFERENCES user_account(id),
  PRIMARY KEY (location_id, user_account_id)
);