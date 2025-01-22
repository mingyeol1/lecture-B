package com.example.lecture_B.dto;

import lombok.Data;
import lombok.ToString;


@Data
@ToString
public class ModifyDTO {
    private String userId;
    private String email;
    private String phoneNum;
    private String nickname;
    private String profileImage;
    private String userRole;
    private String userPw;  // 비밀번호 변경시 필요한 필드 추가. 없으면 매핑시 변경이 불가.
}
