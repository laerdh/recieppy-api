CREATE TABLE location (
  id SERIAL PRIMARY KEY,
  name VARCHAR(64) NOT NULL,
  address VARCHAR(64),
  created_by SERIAL NOT NULL,
  invite_code VARCHAR(6),
  created DATE NOT NULL
);
