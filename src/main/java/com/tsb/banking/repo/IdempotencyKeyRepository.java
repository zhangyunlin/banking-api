package com.tsb.banking.repo;

import com.tsb.banking.model.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, Long> {

    Optional<IdempotencyKey> findByScopeAndKey(String scope, String key);

}
