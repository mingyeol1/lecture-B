package com.example.lecture_B.controller;

import com.example.lecture_B.dto.SignUpDTO;
import com.example.lecture_B.dto.UserDTO;
import com.example.lecture_B.entity.User;
import com.example.lecture_B.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.lecture_B.service.UserService;


@RestController
@Log4j2
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class UserController {

    private final UserService userService;
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("signUp")
    public ResponseEntity<?> signUp(@RequestBody SignUpDTO signUpDTO){
        try {
            User user = userService.signUp(signUpDTO);
            return ResponseEntity.ok(user);
        }catch (UserService.UseridException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("회원가입중 오류발생.");
        }
    }
}
