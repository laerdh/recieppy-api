ALTER TABLE tag ADD COLUMN location_id BIGINT;
ALTER TABLE tag ADD CONSTRAINT tag_location_id_fkey FOREIGN KEY (location_id) REFERENCES location(id);