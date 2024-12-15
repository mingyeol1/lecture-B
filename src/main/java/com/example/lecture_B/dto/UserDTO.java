package com.example.lecture_B.dto;

import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String userId;
    private String phoneNum;
    private String email;
    private String nickname;
    private String profileImage;
    private String userRole;
    private boolean del;
}