package com.example.lecture_B.controller;

import com.example.lecture_B.dto.SignUpDTO;
import com.example.lecture_B.dto.UserDTO;
import com.example.lecture_B.entity.User;
import com.example.lecture_B.repository.UserRepository;
import com.example.lecture_B.security.CustomUserDetailService;
import com.example.lecture_B.util.JwtUtil;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.example.lecture_B.service.UserService;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;


@RestController
@Log4j2
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailService customUserDetailService;
    private final JwtUtil jwtUtil;

    @PostMapping("signUp")
    public ResponseEntity<?> signUp(@RequestBody SignUpDTO signUpDTO){
        try {
            User user = userService.signUp(signUpDTO);
            //회원가입 한 user 값을 리턴.
            return ResponseEntity.ok(user);
        }catch (UserService.UseridException e){
            //예외 메시지에 따라 다른 응답반환.
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/signIn")
    public void getSignIn(){
        log.info("로그인 접근");
    }

    @PostMapping("/signIn")
    public ResponseEntity<?> signIn(@RequestBody SignUpDTO dto, HttpSession session) {
        User user = userService.signIn(dto.getUserId(), dto.getUserPw());

        if (user != null) {
            // UserDetails 생성
            UserDetails userDetails = customUserDetailService.loadUserByUsername(dto.getUserId());

            // Authentication 생성
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );

            // SecurityContext에 인증 정보 설정
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // JWT 생성
            String jwt = JwtUtil.createToken(authentication);

            return ResponseEntity.ok("로그인 성공: " + jwt);
        } else {
            log.info("아이디 없을지도.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("아이디 및 비밀번호 오류");
        }
    }



    @GetMapping("modify")
    public ResponseEntity<?> getUserDetail(){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();

        User detail = userService.userDetail(authentication.getName());

        return ResponseEntity.ok(detail);
    }

}
