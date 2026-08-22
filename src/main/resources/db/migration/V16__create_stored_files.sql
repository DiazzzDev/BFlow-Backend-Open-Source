CREATE TABLE stored_files (
    id UUID PRIMARY KEY,

    user_id UUID NOT NULL,

    object_key VARCHAR(512) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(255) NOT NULL,
    size_bytes BIGINT NOT NULL,

    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_stored_file_user
    FOREIGN KEY (user_id)
    REFERENCES users(id)
    ON DELETE CASCADE
);

CREATE UNIQUE INDEX idx_stored_file_object_key
    ON stored_files(object_key);

CREATE INDEX idx_stored_file_user
    ON stored_files(user_id);

CREATE INDEX idx_stored_file_user_status
    ON stored_files(user_id, status);
