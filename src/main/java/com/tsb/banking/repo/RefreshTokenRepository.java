package com.tsb.banking.repo;

import com.tsb.banking.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByJti(String jti);

    boolean existsByJti(String jti);

    long deleteByExpiresAtBefore(Instant cutoff);

}
