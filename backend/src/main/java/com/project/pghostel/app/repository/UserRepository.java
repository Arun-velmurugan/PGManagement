package com.project.pghostel.app.repository;

import com.project.pghostel.app.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    List<User> findByRoleAndStatus(String role, String status);

    // List<User> findByStatus(String status);

    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);
}