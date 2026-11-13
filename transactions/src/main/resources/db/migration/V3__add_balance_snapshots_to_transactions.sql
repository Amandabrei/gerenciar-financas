ALTER TABLE financial_transactions
    ADD COLUMN balance_before NUMERIC(19, 2);

ALTER TABLE financial_transactions
    ADD COLUMN balance_after NUMERIC(19, 2);