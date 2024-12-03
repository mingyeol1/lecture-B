package com.example.lecture_B.controller;

import com.example.lecture_B.dto.SignUpDTO;
import com.example.lecture_B.dto.UserDTO;
import com.example.lecture_B.entity.User;
import com.example.lecture_B.repository.UserRepository;
import com.example.lecture_B.security.CustomUserDetailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.example.lecture_B.service.UserService;


@RestController
@Log4j2
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailService customUserDetailService;

    @PostMapping("signUp")
    public ResponseEntity<?> signUp(@RequestBody SignUpDTO signUpDTO){
        try {
            User user = userService.signUp(signUpDTO);
            //회원가입 한 user 값을 리턴.
            return ResponseEntity.ok(user);
        }catch (UserService.UseridException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("회원가입중 오류발생.");
        }
    }

    @GetMapping("/signIn")
    public void getSignIn(){
        log.info("로그인 접근");
    }

    @PostMapping("/signIn")
    public ResponseEntity<?> signIn(@RequestBody SignUpDTO dto){
        User user = userService.signIn(dto.getUserId(), dto.getUserPw());

        if (user != null){
            UserDetails userDetails = customUserDetailService.loadUserByUsername(dto.getUserId());
        }else {
            log.info("아이디 없을지도.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("아이디 및 비밀번호 오류");
        }


        return ResponseEntity.ok(user);
    }

}
