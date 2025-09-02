package com.tsb.banking.model;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * Use an idempotency key to ensure that repeated requests with the same key
 * do not result in duplicate transactions.
 * The combination of scope and key must be unique.
 * Scope can be used to partition keys, e.g., by customer or account.
 *
 * @author zhangyunlin
 */

@Entity
@Table(name = "idempotency_keys", uniqueConstraints = @UniqueConstraint(name = "uk_scope_key", columnNames = {"scope", "ikey"}))
public class IdempotencyKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String scope; // e.g., customer:1

    @Column(name = "ikey", nullable = false, length = 64)
    private String key;

    @Column(name = "payload_hash", nullable = false, length = 64)
    private String payloadHash;

    private Long debitTxnId;
    private Long creditTxnId;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public Long getId() {
        return id;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getPayloadHash() {
        return payloadHash;
    }

    public void setPayloadHash(String payloadHash) {
        this.payloadHash = payloadHash;
    }

    public Long getDebitTxnId() {
        return debitTxnId;
    }

    public void setDebitTxnId(Long debitTxnId) {
        this.debitTxnId = debitTxnId;
    }

    public Long getCreditTxnId() {
        return creditTxnId;
    }

    public void setCreditTxnId(Long creditTxnId) {
        this.creditTxnId = creditTxnId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
