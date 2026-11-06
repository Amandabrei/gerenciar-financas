CREATE TABLE financial_transactions (
                                        id UUID NOT NULL PRIMARY KEY,
                                        amount NUMERIC(19, 2) NOT NULL,
                                        category VARCHAR(100),
                                        created_at TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
                                        description VARCHAR(500),
                                        destination_user_id UUID,
                                        original_transaction_id UUID,
                                        owner_id UUID NOT NULL,
                                        rejection_reason VARCHAR(500),
                                        status VARCHAR(30) NOT NULL,
                                        type VARCHAR(30) NOT NULL
);