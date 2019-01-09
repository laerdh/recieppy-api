CREATE TABLE user_recipe_list (
  user_id SERIAL NOT NULL REFERENCES "user"(id),
  recipe_list SERIAL NOT NULL REFERENCES recipe_list(id),
  PRIMARY KEY (user_id, recipe_list)
);