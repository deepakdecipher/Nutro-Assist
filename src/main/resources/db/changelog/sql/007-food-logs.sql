--liquibase formatted sql

--changeset nutro-assist:007-food-logs
CREATE TABLE food_logs (
    id                 BIGSERIAL    PRIMARY KEY,
    user_id            BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    plan_day_id        BIGINT       NOT NULL REFERENCES plan_days(id) ON DELETE CASCADE,
    plan_meal_id       BIGINT       NOT NULL REFERENCES plan_meals(id) ON DELETE CASCADE,
    meal_type          VARCHAR(20)  NOT NULL,
    food_name          VARCHAR(200) NOT NULL,
    quantity_description VARCHAR(100),
    calories_consumed  INT          NOT NULL DEFAULT 0,
    protein_g          NUMERIC(6,1) DEFAULT 0,
    carbs_g            NUMERIC(6,1) DEFAULT 0,
    fat_g              NUMERIC(6,1) DEFAULT 0,
    logged_at          TIMESTAMP    NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, plan_meal_id)
);

CREATE INDEX idx_food_logs_user_day ON food_logs(user_id, plan_day_id);
