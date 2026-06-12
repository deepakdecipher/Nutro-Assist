--liquibase formatted sql

--changeset nutro-assist:001-admin-seed-marker
-- Admin user is seeded via AdminUserInitializer.java (Spring ApplicationRunner)
-- This changeset exists as a Liquibase migration marker only.
SELECT 1
