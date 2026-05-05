-- Добавляем колонку description если её нет
ALTER TABLE transaction_history
ADD COLUMN IF NOT EXISTS description VARCHAR(500);

-- Добавляем колонку version если её нет (на всякий случай)
ALTER TABLE bonus_card
ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0 NOT NULL;

-- Обновляем существующие записи
UPDATE bonus_card SET version = 0 WHERE version IS NULL;