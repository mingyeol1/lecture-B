package com.example.lecture_B.repository;

import com.example.lecture_B.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
