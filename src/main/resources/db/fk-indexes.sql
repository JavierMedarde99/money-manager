-- Foreign key indexes (issue #93).
--
-- Hibernate's `ddl-auto=update` does NOT add missing indexes to already existing tables,
-- and production runs with `ddl-auto=validate`, so apply this script once per environment
-- to index the foreign key columns:
--
--   psql "$POSTGRESQL_URL" -f src/main/resources/db/fk-indexes.sql
--
-- It is idempotent (CREATE INDEX IF NOT EXISTS), so it is safe to run repeatedly.
-- Fresh dev databases already get these indexes from the @Index annotations on the
-- JPA entities in infrastructure/persistance/entity.

CREATE INDEX IF NOT EXISTS idx_categories_user_id ON categories (user_id);

CREATE INDEX IF NOT EXISTS idx_debts_user_id ON debts (user_id);

CREATE INDEX IF NOT EXISTS idx_payments_debt_id ON payments (debt_id);

CREATE INDEX IF NOT EXISTS idx_transactions_user_id ON transactions (user_id);

CREATE INDEX IF NOT EXISTS idx_transactions_category_id ON transactions (category_id);

-- savings has no separate user_id index on purpose: the unique constraint on
-- (user_id, year, month) already provides an index whose leading column is user_id,
-- which covers both findByUser and findByUserAndYearAndMonth.
