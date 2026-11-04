package com.gerenciar.financas.transactions.application.port.in;

import com.gerenciar.financas.transactions.domain.model.Transaction;

import java.math.BigDecimal;
import java.util.UUID;

public interface CreateTransactionUseCase {

    Transaction createDeposit(
            UUID ownerId,
            BigDecimal amount,
            String description
    );

    Transaction createWithdrawal(
            UUID ownerId,
            BigDecimal amount,
            String description
    );

    Transaction createTransfer(
            UUID ownerId,
            UUID destinationUserId,
            BigDecimal amount,
            String description
    );

    Transaction createPurchase(
            UUID ownerId,
            BigDecimal amount,
            String description,
            String category
    );
}
