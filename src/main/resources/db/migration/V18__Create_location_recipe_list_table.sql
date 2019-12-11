CREATE TABLE location_recipe_list (
  location_id SERIAL NOT NULL REFERENCES location(id),
  recipe_list_id SERIAL NOT NULL REFERENCES recipe_list(id)
);