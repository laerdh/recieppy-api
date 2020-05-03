ALTER TABLE location
    ALTER COLUMN created TYPE TIMESTAMP;

ALTER TABLE location
    ALTER COLUMN created SET DEFAULT now();

ALTER TABLE recipe
    ALTER COLUMN created SET DEFAULT now();

ALTER TABLE recipe_list
    ALTER COLUMN created SET DEFAULT now();

ALTER TABLE shared_recipe
    ALTER COLUMN created SET DEFAULT now();

ALTER TABLE shared_recipe_list
    ALTER COLUMN created SET DEFAULT now();

ALTER TABLE user_account
    ALTER COLUMN created SET DEFAULT now();

