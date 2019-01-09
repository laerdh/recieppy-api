CREATE TABLE recipe (
  id SERIAL PRIMARY KEY,
  title VARCHAR NOT NULL,
  url VARCHAR,
  image_url VARCHAR,
  site VARCHAR,
  recipe_list_id BIGINT REFERENCES recipe_list(id)
);