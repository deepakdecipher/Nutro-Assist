--liquibase formatted sql

--changeset nutro-assist:005-diet-plan-templates splitStatements:false
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='public' AND table_name='diet_plan_templates'
CREATE TABLE IF NOT EXISTS diet_plan_templates (
    id          BIGSERIAL    PRIMARY KEY,
    name        VARCHAR(200) NOT NULL,
    goal        VARCHAR(30)  NOT NULL,
    total_days  INT          NOT NULL DEFAULT 30,
    uploaded_by BIGINT       REFERENCES users(id) ON DELETE SET NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

--changeset nutro-assist:005-template-days splitStatements:false
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='public' AND table_name='template_days'
CREATE TABLE IF NOT EXISTS template_days (
    id           BIGSERIAL PRIMARY KEY,
    template_id  BIGINT    NOT NULL REFERENCES diet_plan_templates(id) ON DELETE CASCADE,
    day_number   INT       NOT NULL,
    UNIQUE (template_id, day_number)
);

--changeset nutro-assist:005-template-meals splitStatements:false
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='public' AND table_name='template_meals'
CREATE TABLE IF NOT EXISTS template_meals (
    id            BIGSERIAL    PRIMARY KEY,
    day_id        BIGINT       NOT NULL REFERENCES template_days(id) ON DELETE CASCADE,
    meal_type     VARCHAR(20)  NOT NULL,
    meal_name     VARCHAR(200) NOT NULL,
    description   TEXT,
    calories      INT          NOT NULL DEFAULT 0,
    protein_g     NUMERIC(6,1) DEFAULT 0,
    carbs_g       NUMERIC(6,1) DEFAULT 0,
    fat_g         NUMERIC(6,1) DEFAULT 0,
    display_order INT          NOT NULL DEFAULT 0
);

--changeset nutro-assist:005-template-meals-add-macros splitStatements:false
ALTER TABLE template_meals ADD COLUMN IF NOT EXISTS protein_g     NUMERIC(6,1) DEFAULT 0;
ALTER TABLE template_meals ADD COLUMN IF NOT EXISTS carbs_g       NUMERIC(6,1) DEFAULT 0;
ALTER TABLE template_meals ADD COLUMN IF NOT EXISTS fat_g         NUMERIC(6,1) DEFAULT 0;
ALTER TABLE template_meals ADD COLUMN IF NOT EXISTS display_order INT          NOT NULL DEFAULT 0;
