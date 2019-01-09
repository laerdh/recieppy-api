CREATE TABLE recipe_tag (
  recipe_id SERIAL NOT NULL REFERENCES recipe(id),
  tag_id BIGINT NOT NULL REFERENCES tag(id),
  PRIMARY KEY (recipe_id, tag_id)
);