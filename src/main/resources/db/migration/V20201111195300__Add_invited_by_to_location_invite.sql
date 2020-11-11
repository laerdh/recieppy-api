ALTER TABLE location_invite
    ADD COLUMN invited_by BIGINT NOT NULL references user_account(id);