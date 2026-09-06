ALTER TABLE users
    ADD COLUMN name_source VARCHAR(20) NOT NULL DEFAULT 'GOOGLE';

-- Users who already have a name diverging from what Google would send
-- can't be distinguished retroactively without an audit trail, so every
-- existing row defaults to GOOGLE (sync can overwrite). Only names set
-- from this point forward via PATCH /me will be protected.