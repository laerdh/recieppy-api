ALTER TABLE user_recipe_list
    DROP CONSTRAINT user_account_recipe_list_user_id_fkey,
    DROP CONSTRAINT user_recipe_list_recipe_list_fkey;

DROP TABLE user_recipe_list;