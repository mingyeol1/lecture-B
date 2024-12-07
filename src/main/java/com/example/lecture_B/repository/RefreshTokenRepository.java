package com.example.lecture_B.repository;

import com.example.lecture_B.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    void deleteByToken(String token);

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.token = :newToken WHERE rt.userId = :username")
    int updateTokenByUsername(@Param("newToken") String newToken, @Param("username") String username);

    Optional<RefreshToken> findByUserId(String userId);

    void deleteByUserId(String userid);
}
