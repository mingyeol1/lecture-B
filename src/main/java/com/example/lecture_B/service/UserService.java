package com.example.lecture_B.service;

import com.example.lecture_B.dto.ModifyDTO;
import com.example.lecture_B.dto.SignUpDTO;
import com.example.lecture_B.dto.TokenDTO;
import com.example.lecture_B.dto.UserDTO;
import com.example.lecture_B.entity.User;

public interface UserService {

    class UseridException extends Exception {   //오류를 직접 만들고 던짐.
        public UseridException(String message) {
            super(message);
        }
    }

    User signUp (SignUpDTO dto) throws UseridException;

    User signIn (String id, String pw) throws UseridException ;

    User userDetail (String id);

    // refreshToken 토큰 검증 및  accessToken 재발급 받는 메서드
    TokenDTO tokenReissue(String refreshToken);

    void modify(ModifyDTO modifyDTO) throws UseridException;

    void userRemove(String userId);

    UserDTO updateProfileImage(String userId, String imageUrl);
}
