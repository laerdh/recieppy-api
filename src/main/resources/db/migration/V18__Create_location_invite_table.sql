CREATE TABLE location_invite (
  id SERIAL PRIMARY KEY NOT NULL,
  invite_code VARCHAR(6) NOT NULL,
  invited_id SERIAL,
  created DATE NOT NULL
);