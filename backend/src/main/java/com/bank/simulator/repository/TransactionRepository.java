package com.bank.simulator.repository;

import com.bank.simulator.entity.TransactionEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {

    List<TransactionEntity> findBySenderAccountNumberOrReceiverAccountNumberOrderByCreatedDateDesc(
            String senderAccountNumber, String receiverAccountNumber);

    List<TransactionEntity> findAllByOrderByCreatedDateDesc();

    Optional<TransactionEntity> findByTransactionId(String transactionId);

    /**
     * Fetches the most recent transactionId that starts with the given date prefix
     * Used to determine counter for TXN_YYYYMMDDNNN format
     */
    @Query("SELECT t.transactionId FROM TransactionEntity t WHERE t.transactionId LIKE :prefix% ORDER BY t.transactionId DESC")
    List<String> findLastTransactionIdForDate(@Param("prefix") String prefix, Pageable pageable);
}
