-- Таблица клиентских карт
CREATE TABLE IF NOT EXISTS bonus_card (
    id BIGSERIAL PRIMARY KEY,
    card_number VARCHAR(50) UNIQUE NOT NULL,
    balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Таблица истории операций (со всеми колонками)
CREATE TABLE IF NOT EXISTS transaction_history (
    id BIGSERIAL PRIMARY KEY,
    card_number VARCHAR(50) NOT NULL,
    operation_type VARCHAR(20) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    balance_before DECIMAL(15,2) NOT NULL,
    balance_after DECIMAL(15,2) NOT NULL,
    reference_id VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Индексы для оптимизации SELECT
CREATE INDEX IF NOT EXISTS idx_history_card_created ON transaction_history(card_number, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_history_reference ON transaction_history(reference_id);
CREATE INDEX IF NOT EXISTS idx_card_number ON bonus_card(card_number);
CREATE INDEX IF NOT EXISTS idx_history_created ON transaction_history(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_history_card_time ON transaction_history(card_number, created_at DESC);

-- Комментарии
COMMENT ON TABLE bonus_card IS 'Таблица бонусных карт клиентов';
COMMENT ON COLUMN bonus_card.card_number IS 'Номер карты (уникальный)';
COMMENT ON COLUMN bonus_card.balance IS 'Текущий баланс бонусов';

COMMENT ON TABLE transaction_history IS 'История операций с бонусами';
COMMENT ON COLUMN transaction_history.operation_type IS 'Тип операции: EARN, BURN, REFUND_EARN, REFUND_BURN';
COMMENT ON COLUMN transaction_history.reference_id IS 'ID внешней операции (заказ, возврат)';