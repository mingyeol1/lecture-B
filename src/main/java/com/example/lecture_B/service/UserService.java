package com.example.lecture_B.service;

import com.example.lecture_B.dto.SignUpDTO;
import com.example.lecture_B.entity.User;

public interface UserService {

    class UseridException extends Exception {
        public UseridException(String message) {
            super(message);
        }
    }

    User signUp (SignUpDTO dto) throws UseridException;

    User signIn (String id, String pw);
}
