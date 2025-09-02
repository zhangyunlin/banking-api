package com.tsb.banking.service;

import com.tsb.banking.exception.BusinessException;
import com.tsb.banking.model.User;
import com.tsb.banking.repo.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PasswordService {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    public PasswordService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Transactional
    public void updatePassword(User user, String rawPassword) {
        if (rawPassword == null || rawPassword.length() < 8) {
            throw new BusinessException("WEAK_PASSWORD", "Password must be at least 8 characters");
        }
        String hash = encoder.encode(rawPassword);
        user.setPasswordHash(hash);
        userRepo.save(user);
    }
}
