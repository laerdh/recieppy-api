CREATE TABLE recipe_list_recipe(
    recipe_list_id BIGINT NOT NULL REFERENCES recipe_list(id),
    recipe_id BIGINT NOT NULL REFERENCES recipe(id),
    PRIMARY KEY (recipe_list_id, recipe_id)
);