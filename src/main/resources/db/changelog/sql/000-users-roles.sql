--liquibase formatted sql

--changeset nutro-assist:000-users splitStatements:false
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='public' AND table_name='users'
CREATE TABLE IF NOT EXISTS users (
    id             BIGSERIAL    PRIMARY KEY,
    user_full_name VARCHAR(255),
    user_name      VARCHAR(255) UNIQUE,
    password       VARCHAR(255),
    email          VARCHAR(255) UNIQUE NOT NULL,
    phone_number   VARCHAR(255) UNIQUE,
    is_verified    BOOLEAN      NOT NULL DEFAULT FALSE
);

--changeset nutro-assist:000-roles splitStatements:false
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='public' AND table_name='roles'
CREATE TABLE IF NOT EXISTS roles (
    id        BIGSERIAL    PRIMARY KEY,
    role_name VARCHAR(255) NOT NULL,
    user_id   BIGINT       REFERENCES users(id) ON DELETE CASCADE
);
