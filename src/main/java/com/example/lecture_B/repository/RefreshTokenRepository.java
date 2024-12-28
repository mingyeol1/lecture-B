package com.example.lecture_B.repository;

import com.example.lecture_B.entity.RefreshToken;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

@Transactional
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    void deleteByToken(String token);

    //데이터베이스의 데이터를 수정 및 삭제하는 쿼리 실행. @Modifying가 없으면 select라고 가정함.
    @Modifying
    //JPQL 지정.
    @Query("UPDATE RefreshToken rt SET rt.token = :newToken WHERE rt.userId = :username")
    int updateTokenByUsername(@Param("newToken") String newToken, @Param("username") String username);

    //로그아웃시 리프레시토큰값을 null값으로 바꾸기 위한 SQL로직
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.token = NULL WHERE rt.userId = :userId")
    void setTokenNullByUserId(@Param("userId") String userId);

    Optional<RefreshToken> findByUserId(String userId);

    void deleteByUserId(String userid);
}
