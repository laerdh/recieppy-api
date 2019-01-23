CREATE TABLE user_role (
  user_id SERIAL NOT NULL REFERENCES user_account(id),
  role_id SERIAL NOT NULL REFERENCES role(id),
  PRIMARY KEY (user_id, role_id)
);