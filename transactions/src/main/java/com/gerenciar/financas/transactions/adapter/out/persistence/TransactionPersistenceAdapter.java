package com.gerenciar.financas.transactions.adapter.out.persistence;

import com.gerenciar.financas.transactions.adapter.out.persistence.entity.TransactionEntity;
import com.gerenciar.financas.transactions.adapter.out.persistence.mapper.TransactionMapper;
import com.gerenciar.financas.transactions.adapter.out.persistence.repository.SpringDataTransactionRepository;
import com.gerenciar.financas.transactions.application.port.out.TransactionRepositoryPort;
import com.gerenciar.financas.transactions.domain.model.Transaction;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class TransactionPersistenceAdapter implements TransactionRepositoryPort {

    private final SpringDataTransactionRepository repository;
    private final TransactionMapper mapper;

    public TransactionPersistenceAdapter(
            SpringDataTransactionRepository repository,
            TransactionMapper mapper
    ) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Transaction save(Transaction transaction) {
        TransactionEntity entity = mapper.toEntity(transaction);
        TransactionEntity savedEntity = repository.save(entity);

        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Transaction> findById(UUID id) {
        return repository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<Transaction> findAllByOwnerId(UUID ownerId) {
        return repository.findAllByOwnerId(ownerId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByOriginalTransactionId(UUID originalTransactionId) {
        return repository.existsByOriginalTransactionId(originalTransactionId);
    }
}
