ALTER TABLE user_account ADD COLUMN push_token VARCHAR(128) DEFAULT '';
ALTER TABLE user_account ADD COLUMN device_os VARCHAR(10) DEFAULT '';
