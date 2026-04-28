package com.authsystem.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.authsystem.entity.LoginHistory;
import com.authsystem.entity.User;

@Repository
public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {

    List<LoginHistory> findByUserOrderByLoginTimeDesc(User user);
    
    List<LoginHistory> findTop10ByUserOrderByLoginTimeDesc(User user);
}

