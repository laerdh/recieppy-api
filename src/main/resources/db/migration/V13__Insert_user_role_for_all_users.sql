INSERT INTO user_role (user_id, role_id)
SELECT id, (SELECT id FROM role WHERE name = 'USER') FROM user_account;