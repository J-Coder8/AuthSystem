package com.authsystem.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.authsystem.entity.AuthenticatorAccount;
import com.authsystem.entity.User;

public interface AuthenticatorAccountRepository extends JpaRepository<AuthenticatorAccount, Long> {
    List<AuthenticatorAccount> findByUserOrderByCreatedAtDesc(User user);
    Optional<AuthenticatorAccount> findByIdAndUser(Long id, User user);
}

