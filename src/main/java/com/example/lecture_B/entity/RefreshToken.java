package com.example.lecture_B.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // 데이터베이스에서 각 Refresh Token을 고유하게 식별
    private Long id;
    // 실제 Refresh Token 값 저장
    @Column(length = 1000)
    private String token;
    // Refresh Token이 속한 사용자의 Id
    private String userId;
}

