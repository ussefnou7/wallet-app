-- Drop enabled column if it exists (for databases that have the old schema)
ALTER TABLE users DROP COLUMN IF EXISTS enabled;