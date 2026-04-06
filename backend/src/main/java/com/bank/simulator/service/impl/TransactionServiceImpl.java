package com.bank.simulator.service.impl;

import com.bank.simulator.entity.AccountEntity;
import com.bank.simulator.entity.CustomerEntity;
import com.bank.simulator.entity.TransactionEntity;
import com.bank.simulator.exception.BusinessException;
import com.bank.simulator.repository.AccountRepository;
import com.bank.simulator.repository.TransactionRepository;
import com.bank.simulator.service.NotificationService;
import com.bank.simulator.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final NotificationService notificationService;

    private static final DateTimeFormatter TXN_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final Object TXN_ID_LOCK = new Object();

    @Override
    @Transactional
    public String createTransaction(Map<String, Object> payload) {

        // --- Extract & validate input ---
        String senderAccountNumber = getStr(payload, "senderAccountNumber");
        String receiverAccountNumber = getStr(payload, "receiverAccountNumber");
        String pin = getStr(payload, "pin");
        String description = getStr(payload, "description");
        String transactionType = payload.containsKey("transactionType") ? getStr(payload, "transactionType") : "ONLINE";

        BigDecimal amount;
        try {
            amount = new BigDecimal(payload.get("amount").toString());
        } catch (Exception e) {
            throw new BusinessException("Invalid amount value");
        }

        if (senderAccountNumber == null || senderAccountNumber.isBlank())
            throw new BusinessException("Sender account number is required");
        if (receiverAccountNumber == null || receiverAccountNumber.isBlank())
            throw new BusinessException("Receiver account number is required");
        if (pin == null || !pin.matches("\\d{6}"))
            throw new BusinessException("Transaction PIN must be exactly 6 digits");
        if (amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new BusinessException("Transaction amount must be greater than zero");

        // --- Rule 1: Self-transfer prevention ---
        if (senderAccountNumber.equals(receiverAccountNumber)) {
            throw new BusinessException("Self-transfer is not allowed. Sender and receiver accounts cannot be the same.");
        }

        // --- Fetch sender account ---
        AccountEntity senderAccount = accountRepository.findByAccountNumber(senderAccountNumber)
                .orElseThrow(() -> new BusinessException("Sender account not found: " + senderAccountNumber));

        // --- Rule 2: Sender account active status ---
        if (!"ACTIVE".equalsIgnoreCase(senderAccount.getStatus())) {
            throw new BusinessException("Your account is not active. Transactions are not permitted.");
        }

        // --- Get sender's customer for PIN validation ---
        CustomerEntity senderCustomer = senderAccount.getCustomer();
        if (senderCustomer == null) {
            throw new BusinessException("Customer record not found for sender account.");
        }

        // --- Rule 3: PIN validation ---
        if (!pin.equals(senderCustomer.getCustomerPin())) {
            throw new BusinessException("Invalid transaction PIN. Please check your PIN and try again.");
        }

        // --- Fetch receiver account ---
        AccountEntity receiverAccount = accountRepository.findByAccountNumber(receiverAccountNumber)
                .orElseThrow(() -> new BusinessException("Receiver account not found: " + receiverAccountNumber));

        // --- Rule 4: Receiver account active status ---
        if (!"ACTIVE".equalsIgnoreCase(receiverAccount.getStatus())) {
            throw new BusinessException("Receiver account is not active. Transaction cannot be processed.");
        }

        // --- Rule 5: Sufficient balance check ---
        if (senderAccount.getAmount().compareTo(amount) < 0) {
            throw new BusinessException(
                "Insufficient balance. Available: ₹" + senderAccount.getAmount() + ", Required: ₹" + amount);
        }

        // --- Snapshot customer email data NOW (before save/flush changes state) ---
        CustomerEntity receiverCustomer = receiverAccount.getCustomer();
        String senderEmail   = senderCustomer.getEmail();
        String senderName    = senderCustomer.getName();
        String senderBank    = senderAccount.getBankName();
        String receiverEmail = receiverCustomer != null ? receiverCustomer.getEmail() : null;
        String receiverName  = receiverCustomer != null ? receiverCustomer.getName() : null;
        String receiverBank  = receiverAccount.getBankName();

        // --- Generate TXN ID ---
        String transactionId = generateTransactionId();

        // --- Update balances ---
        senderAccount.setAmount(senderAccount.getAmount().subtract(amount));
        receiverAccount.setAmount(receiverAccount.getAmount().add(amount));
        accountRepository.save(senderAccount);
        accountRepository.save(receiverAccount);

        // --- Record transaction ---
        TransactionEntity transaction = TransactionEntity.builder()
                .transactionId(transactionId)
                .account(senderAccount)
                .senderAccountNumber(senderAccountNumber)
                .receiverAccountNumber(receiverAccountNumber)
                .amount(amount)
                .transactionType(transactionType)
                .description(description)
                .build();

        transactionRepository.save(transaction);
        log.info("Transaction completed: {} | ₹{} | {} → {}", transactionId, amount, senderAccountNumber, receiverAccountNumber);

        // --- Send email notifications only after successful commit ---
        // Pass primitive/String values only — no JPA entities — to avoid LazyInitializationException
        scheduleTransactionEmailsAfterCommit(
                senderEmail, senderName, senderBank,
                receiverEmail, receiverName, receiverBank,
                senderAccountNumber, receiverAccountNumber,
                amount, transactionId
        );

        return transactionId;
    }

    private void scheduleTransactionEmailsAfterCommit(
            String senderEmail, String senderName, String senderBank,
            String receiverEmail, String receiverName, String receiverBank,
            String senderAccountNumber, String receiverAccountNumber,
            BigDecimal amount, String transactionId) {

        if (TransactionSynchronizationManager.isSynchronizationActive()
                && TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    log.debug("Transaction {} committed. Triggering email notifications.", transactionId);
                    notificationService.sendTransactionNotificationsAsync(
                            senderEmail, senderName, senderBank,
                            receiverEmail, receiverName, receiverBank,
                            senderAccountNumber, receiverAccountNumber,
                            amount, transactionId
                    );
                }
            });
            return;
        }

        log.warn("No transaction synchronization available for {}. Sending notifications immediately.", transactionId);
        notificationService.sendTransactionNotificationsAsync(
                senderEmail, senderName, senderBank,
                receiverEmail, receiverName, receiverBank,
                senderAccountNumber, receiverAccountNumber,
                amount, transactionId
        );
    }

    @Override
    public List<TransactionEntity> getTransactionsByAccount(String accountNumber) {
        return transactionRepository.findBySenderAccountNumberOrReceiverAccountNumberOrderByCreatedDateDesc(
                accountNumber, accountNumber);
    }

    @Override
    public List<TransactionEntity> getAllTransactions() {
        return transactionRepository.findAllByOrderByCreatedDateDesc();
    }

    @Override
    @Transactional
    public void deleteTransaction(String transactionId) {
        TransactionEntity txn = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new BusinessException(
                    "Transaction not found: " + transactionId, HttpStatus.NOT_FOUND));
        transactionRepository.delete(txn);
        log.info("Transaction deleted: {}", transactionId);
    }

    private String generateTransactionId() {
        synchronized (TXN_ID_LOCK) {
            String dateStr = LocalDate.now().format(TXN_DATE_FORMAT);
            String prefix = "TXN_" + dateStr;

            List<String> lastIds = transactionRepository.findLastTransactionIdForDate(
                    prefix, PageRequest.of(0, 1));

            int counter = 1;
            if (!lastIds.isEmpty()) {
                String lastId = lastIds.get(0);
                try {
                    String counterStr = lastId.substring(prefix.length());
                    counter = Integer.parseInt(counterStr) + 1;
                } catch (Exception e) {
                    log.warn("Could not parse last TXN ID counter from: {}", lastIds.get(0));
                    counter = (int)(transactionRepository.count() + 1);
                }
            }

            return String.format("%s%03d", prefix, counter);
        }
    }

    private String getStr(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString().trim() : null;
    }
}
