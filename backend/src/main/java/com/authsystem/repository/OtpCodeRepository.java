package com.authsystem.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.authsystem.entity.OtpCode;
import com.authsystem.entity.User;

@Repository
public interface OtpCodeRepository extends JpaRepository<OtpCode, Long> {

    Optional<OtpCode> findByUserAndCodeAndUsedFalse(User user, String code);
    
    Optional<OtpCode> findTopByUserAndUsedFalseOrderByCreatedAtDesc(User user);

    @Modifying
    @Transactional
    void deleteByUser(User user);

    @Modifying
    @Transactional
    void deleteByUserAndUsedTrue(User user);
}
