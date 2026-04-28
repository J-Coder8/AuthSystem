package com.authsystem.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.authsystem.entity.TotpConfig;
import com.authsystem.entity.User;

@Repository
public interface TotpConfigRepository extends JpaRepository<TotpConfig, Long> {
    Optional<TotpConfig> findByUser(User user);
    Optional<TotpConfig> findByUserUsername(String username);
    void deleteByUser(User user);
}

