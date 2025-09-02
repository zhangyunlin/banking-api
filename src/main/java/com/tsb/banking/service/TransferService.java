package com.tsb.banking.service;

import com.tsb.banking.api.dto.TransferRequestDto;
import com.tsb.banking.api.dto.TransferResponseDto;
import com.tsb.banking.exception.BusinessException;
import com.tsb.banking.exception.NotFoundException;
import com.tsb.banking.model.Account;
import com.tsb.banking.model.IdempotencyKey;
import com.tsb.banking.model.Transaction;
import com.tsb.banking.repo.AccountRepository;
import com.tsb.banking.repo.IdempotencyKeyRepository;
import com.tsb.banking.repo.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

/**
 * @author zhangyunlin
 */
@Service
public class TransferService {

    private final AccountRepository accountRepo;
    private final TransactionRepository txnRepo;
    private final IdempotencyKeyRepository idemRepo;

    public TransferService(AccountRepository accountRepo, TransactionRepository txnRepo, IdempotencyKeyRepository idemRepo) {
        this.accountRepo = accountRepo;
        this.txnRepo = txnRepo;
        this.idemRepo = idemRepo;
    }

    private static String sha256(String s) {
        try {
            var md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(s.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public TransferResponseDto transfer(TransferRequestDto request) {

        // check accounts
        if (request.fromAccountId().equals(request.toAccountId())) {
            throw new BusinessException("TRANSFER_SAME_ACCOUNT", "fromAccount and toAccount must be different");
        }

        // check transfer amount precision
        if (request.amount().scale() > 2) {
            throw new BusinessException("AMOUNT_PRECISION", "amount must have at most 2 decimal places");
        }

        // Idempotency (scope per customer)
        String scope = "customer:" + request.customerId();
        if (request.idempotencyKey() != null && !request.idempotencyKey().isBlank()) {
            String payloadHash = sha256(request.fromAccountId()+":"+request.toAccountId()+":"+request.amount()+":"+request.currency()+":"+String.valueOf(request.memo()));

            // Check if idem key exists
            var existing = idemRepo.findByScopeAndKey(scope, request.idempotencyKey());
            if (existing.isPresent()) {
                var idem = existing.get();
                if (!idem.getPayloadHash().equals(payloadHash)) {
                    throw new BusinessException("IDEMPOTENCY_Exception", "Idempotency key has been used with different payload");
                }
                // return current balances + saved txn ids
                var from = accountRepo.findById(request.fromAccountId()).orElseThrow(() -> new NotFoundException("Account "+request.fromAccountId()+" not found"));
                var to = accountRepo.findById(request.toAccountId()).orElseThrow(() -> new NotFoundException("Account "+request.toAccountId()+" not found"));
                return new TransferResponseDto(idem.getDebitTxnId(), idem.getCreditTxnId(), from.getBalance(), to.getBalance());
            }
        }

        // Lock accounts in id order to avoid deadlock
        Long firstId = request.fromAccountId() < request.toAccountId() ? request.fromAccountId() : request.toAccountId();
        Long secondId = request.fromAccountId() < request.toAccountId() ? request.toAccountId() : request.fromAccountId();

        Account first = accountRepo.findByIdForUpdate(firstId).orElseThrow(() -> new NotFoundException("Account "+firstId+" not found"));
        Account second = accountRepo.findByIdForUpdate(secondId).orElseThrow(() -> new NotFoundException("Account "+secondId+" not found"));

        Account from = request.fromAccountId().equals(first.getId()) ? first : second;
        Account to   = request.toAccountId().equals(second.getId()) ? second : first;

        // Validate ownership and currency
        if (!from.getCustomer().getId().equals(request.customerId()) || !to.getCustomer().getId().equals(request.customerId())) {
            throw new BusinessException("OWNERSHIP", "Both accounts must belong to the customer");
        }
        if (!from.getCurrency().equals(request.currency()) || !to.getCurrency().equals(request.currency())) {
            throw new BusinessException("CURRENCY_MISMATCH", "Currency must match both accounts");
        }

        // Sufficient funds
        if (from.getBalance().compareTo(request.amount()) < 0) {
            throw new BusinessException("INSUFFICIENT_FUNDS", "Insufficient funds");
        }

        // Apply transfer
        from.setBalance(from.getBalance().subtract(request.amount()));
        to.setBalance(to.getBalance().add(request.amount()));

        // Persist updated balances
        accountRepo.save(from);
        accountRepo.save(to);

        // Create double-entry txns
        Transaction debit = new Transaction();
        debit.setAccount(from);
        debit.setType("DEBIT");
        debit.setAmount(request.amount());
        debit.setCurrency(request.currency());
        debit.setReference(request.memo() == null ? "Internal transfer" : request.memo());
        debit.setCounterparty(to.getNumber());
        txnRepo.save(debit);

        Transaction credit = new Transaction();
        credit.setAccount(to);
        credit.setType("CREDIT");
        credit.setAmount(request.amount());
        credit.setCurrency(request.currency());
        credit.setReference(request.memo() == null ? "Internal transfer" : request.memo());
        credit.setCounterparty(from.getNumber());
        txnRepo.save(credit);

        // Save idempotency record if provided
        if (request.idempotencyKey() != null && !request.idempotencyKey().isBlank()) {
            IdempotencyKey idem = new IdempotencyKey();
            idem.setScope(scope);
            idem.setKey(request.idempotencyKey());
            idem.setPayloadHash(sha256(request.fromAccountId()+":"+request.toAccountId()+":"+request.amount()+":"+request.currency()+":"+String.valueOf(request.memo())));
            idem.setDebitTxnId(debit.getId());
            idem.setCreditTxnId(credit.getId());
            idemRepo.save(idem);
        }

        return new TransferResponseDto(debit.getId(), credit.getId(), from.getBalance(), to.getBalance());
    }
}
