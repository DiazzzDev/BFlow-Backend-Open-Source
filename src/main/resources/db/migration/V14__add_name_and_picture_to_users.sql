ALTER TABLE users
    ADD COLUMN name VARCHAR(255),
    ADD COLUMN picture_url VARCHAR(500),
    ADD COLUMN picture_source VARCHAR(20) NOT NULL DEFAULT 'NONE';

ALTER TABLE users
    ADD CONSTRAINT users_picture_source_check
        CHECK (picture_source IN ('GOOGLE', 'S3', 'NONE'));