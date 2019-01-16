ALTER TABLE "user" RENAME TO user_account;
ALTER TABLE user_recipe_list DROP CONSTRAINT user_recipe_list_user_id_fkey;
ALTER TABLE user_recipe_list
  ADD CONSTRAINT user_account_recipe_list_user_id_fkey
    FOREIGN KEY (user_id) REFERENCES user_account(id) ON DELETE CASCADE ON UPDATE CASCADE;