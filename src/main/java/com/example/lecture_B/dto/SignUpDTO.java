package com.example.lecture_B.dto;

import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.NotFound;

@ToString
@Data
public class SignUpDTO {
    private String userId;
    private String email;
    private String phoneNum;
    private String nickname;
    private String profileImage;
    private String userRole;
    private String userPw;  // 비밀번호 필드
}
