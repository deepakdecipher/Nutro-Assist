--liquibase formatted sql

--changeset nutro-assist:003-user-assessments
CREATE TABLE user_assessments (
    id                    BIGSERIAL    PRIMARY KEY,
    user_id               BIGINT       NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    full_name             VARCHAR(120) NOT NULL,
    age                   INT          NOT NULL,
    gender                VARCHAR(10)  NOT NULL,
    height_cm             NUMERIC(5,1) NOT NULL,
    weight_kg             NUMERIC(5,1) NOT NULL,
    activity_level        VARCHAR(30)  NOT NULL,
    goal                  VARCHAR(30)  NOT NULL,
    dietary_preferences   TEXT,
    allergies             TEXT,
    medical_conditions    TEXT,
    food_interests        TEXT,
    daily_calorie_target  INT          NOT NULL DEFAULT 0,
    completed             BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at            TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMP    NOT NULL DEFAULT NOW()
);
