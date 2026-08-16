-- V2: OAuth provider column — already in V1 (consolidated).
-- This migration is a no-op to preserve checksum history.
-- oauth_provider and nullable password_hash are handled in V1.
SELECT 1;
