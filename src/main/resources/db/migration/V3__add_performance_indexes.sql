-- ============================================
-- Дополнительные индексы для оптимизации SELECT
-- ============================================

-- Индекс для быстрого поиска карт с большим балансом
CREATE INDEX IF NOT EXISTS idx_bonus_card_balance ON bonus_card(balance DESC);

-- Композитный индекс для частых запросов по карте + типу операции
CREATE INDEX IF NOT EXISTS idx_history_card_type ON transaction_history(card_number, operation_type);

-- Частичный индекс для больших операций
CREATE INDEX IF NOT EXISTS idx_history_amount ON transaction_history(amount) WHERE amount > 100;

-- BRIN индекс для дат (экономит место)
CREATE INDEX IF NOT EXISTS idx_history_brin ON transaction_history USING BRIN(created_at) WITH (pages_per_range = 128);

-- Индекс для LIKE поиска
CREATE INDEX IF NOT EXISTS idx_card_number_pattern ON bonus_card(card_number varchar_pattern_ops);