package com.authsystem.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.authsystem.entity.Notification;
import com.authsystem.entity.User;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findTop50ByUserOrderByCreatedAtDesc(User user);
    long countByUserAndReadFalse(User user);
    Optional<Notification> findByIdAndUser(Long id, User user);
}

