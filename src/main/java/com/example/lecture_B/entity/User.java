package com.example.lecture_B.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;
    @Column(nullable = false)
    private String userId; // 로그인용 ID
    @Column(nullable = false)
    private String userPw;
    private String phoneNum;
    @Column(nullable = false)
    private String email;
    @Column(nullable = false)
    private String nickname;
    private String profileImage;
    private boolean del;

    @Enumerated(EnumType.STRING)
    private Set<UserRole> userRole = new HashSet<>(); // Enum: ADMIN, USER, ANA

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;


    //addRole 역할 추가.
    public void addRole (UserRole role){
        this.userRole.add(role);
    }

    //삭제 여부(유저가 계정삭제시 바로 삭제가 되지 않도록. 유예)
    public void changeDel(boolean del){
        this.del = del;
    }

}