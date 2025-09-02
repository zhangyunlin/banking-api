package com.tsb.banking.repo;

import com.tsb.banking.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * @author zhangyunlin
 */
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsernameIgnoreCase(String username);
    Optional<User> findByEmailIgnoreCase(String email);
    Optional<User> findByPhone(String phone);

    default Optional<User> findByIdentifier(String identifier) {

        if (identifier == null){
            return Optional.empty();
        }

        return findByUsernameIgnoreCase(identifier)
                .or(() -> findByEmailIgnoreCase(identifier))
                .or(() -> findByPhone(identifier));
    }
}
