package com.tsb.banking.repo;

import com.tsb.banking.model.OtpToken;
import com.tsb.banking.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {

    Optional<OtpToken> findTopByUserAndPurposeAndConsumedFalseOrderByCreatedAtDesc(User user, String purpose);

    long countByDestinationAndPurposeAndCreatedAtAfter(String destination, String purpose, Instant after);

}
