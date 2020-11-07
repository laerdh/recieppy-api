ALTER TABLE location_invite
    RENAME COLUMN sent TO time_sent;

ALTER TABLE location_invite
    ALTER COLUMN time_sent DROP DEFAULT;