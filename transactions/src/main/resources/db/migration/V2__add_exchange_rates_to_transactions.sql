ALTER TABLE financial_transactions
    ADD COLUMN usd_exchange_rate NUMERIC(19, 6);

ALTER TABLE financial_transactions
    ADD COLUMN usd_quote_date DATE;

ALTER TABLE financial_transactions
    ADD COLUMN eur_exchange_rate NUMERIC(19, 6);

ALTER TABLE financial_transactions
    ADD COLUMN eur_quote_date DATE;