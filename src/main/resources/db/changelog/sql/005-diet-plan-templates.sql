--liquibase formatted sql

--changeset nutro-assist:005-diet-plan-templates
CREATE TABLE diet_plan_templates (
    id          BIGSERIAL    PRIMARY KEY,
    name        VARCHAR(200) NOT NULL,
    goal        VARCHAR(30)  NOT NULL,
    total_days  INT          NOT NULL DEFAULT 30,
    uploaded_by BIGINT       REFERENCES users(id) ON DELETE SET NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

--changeset nutro-assist:005-template-days
CREATE TABLE template_days (
    id           BIGSERIAL PRIMARY KEY,
    template_id  BIGINT    NOT NULL REFERENCES diet_plan_templates(id) ON DELETE CASCADE,
    day_number   INT       NOT NULL,
    UNIQUE (template_id, day_number)
);

--changeset nutro-assist:005-template-meals
CREATE TABLE template_meals (
    id           BIGSERIAL    PRIMARY KEY,
    day_id       BIGINT       NOT NULL REFERENCES template_days(id) ON DELETE CASCADE,
    meal_type    VARCHAR(20)  NOT NULL,
    meal_name    VARCHAR(200) NOT NULL,
    description  TEXT,
    calories     INT          NOT NULL DEFAULT 0,
    protein_g    NUMERIC(6,1) DEFAULT 0,
    carbs_g      NUMERIC(6,1) DEFAULT 0,
    fat_g        NUMERIC(6,1) DEFAULT 0,
    display_order INT         NOT NULL DEFAULT 0
);
