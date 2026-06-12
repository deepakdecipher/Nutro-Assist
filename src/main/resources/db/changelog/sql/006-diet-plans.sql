--liquibase formatted sql

--changeset nutro-assist:006-diet-plans splitStatements:false
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='public' AND table_name='diet_plans'
CREATE TABLE IF NOT EXISTS diet_plans (
    id                    BIGSERIAL    PRIMARY KEY,
    user_id               BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    plan_type             VARCHAR(20)  NOT NULL,
    plan_status           VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    daily_calorie_target  INT          NOT NULL DEFAULT 0,
    start_date            DATE         NOT NULL,
    end_date              DATE         NOT NULL,
    source_template_id    BIGINT       REFERENCES diet_plan_templates(id) ON DELETE SET NULL,
    created_at            TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_diet_plans_user_status ON diet_plans(user_id, plan_status);

--changeset nutro-assist:006-plan-days splitStatements:false
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='public' AND table_name='plan_days'
CREATE TABLE IF NOT EXISTS plan_days (
    id           BIGSERIAL PRIMARY KEY,
    plan_id      BIGINT    NOT NULL REFERENCES diet_plans(id) ON DELETE CASCADE,
    day_number   INT       NOT NULL,
    plan_date    DATE      NOT NULL,
    UNIQUE (plan_id, plan_date)
);

CREATE INDEX IF NOT EXISTS idx_plan_days_plan_date ON plan_days(plan_id, plan_date);

--changeset nutro-assist:006-plan-meals splitStatements:false
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='public' AND table_name='plan_meals'
CREATE TABLE IF NOT EXISTS plan_meals (
    id            BIGSERIAL    PRIMARY KEY,
    plan_day_id   BIGINT       NOT NULL REFERENCES plan_days(id) ON DELETE CASCADE,
    meal_type     VARCHAR(20)  NOT NULL,
    meal_name     VARCHAR(200) NOT NULL,
    description   TEXT,
    calories      INT          NOT NULL DEFAULT 0,
    protein_g     NUMERIC(6,1) DEFAULT 0,
    carbs_g       NUMERIC(6,1) DEFAULT 0,
    fat_g         NUMERIC(6,1) DEFAULT 0,
    display_order INT          NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_plan_meals_day_id ON plan_meals(plan_day_id);
