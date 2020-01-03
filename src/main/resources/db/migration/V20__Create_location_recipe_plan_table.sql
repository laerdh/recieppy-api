CREATE TABLE location_recipe_plan(
    recipe_id SERIAL NOT NULL REFERENCES recipe(id),
    location_id SERIAL NOT NULL REFERENCES location(id),
    date DATE NOT NULL,
    PRIMARY KEY (recipe_id, location_id, date)
);