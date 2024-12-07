package com.example.lecture_B.controller;

import com.example.lecture_B.dto.TokenDTO;
import com.example.lecture_B.entity.User;
import com.example.lecture_B.repository.RefreshTokenRepository;
import com.example.lecture_B.security.filter.excption.UserNotFoundException;
import com.example.lecture_B.service.UserService;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final RefreshTokenRepository refreshTokenRepository;

    @GetMapping("modify")
    public ResponseEntity<?> getUserDetail(){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();

        User detail = userService.userDetail(authentication.getName());

        return ResponseEntity.ok(detail);
    }

    @GetMapping("/token")
    public ResponseEntity<?> readToken(@RequestHeader("Authorization") String token) {
        try {
            // Bearer 토큰에서 "Bearer " 부분 제거
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            // payload 반환
            Map<String, Object> Message = new HashMap<>();

            Message.put("message", "유효한 토큰입니다.");

            return ResponseEntity.ok(Message);

        } catch (UserNotFoundException e) {
            return ResponseEntity.status(403).body("유저 정보가 맞지 않습니다.");
        } catch (JwtException e) {
            return ResponseEntity.status(401).body("토큰이 유효하지 않습니다.");
        }
    }

    // refreshToken으로 accessToken을 재발급하는 메서드, accessToken과 refreshToken값을 같이 JSON 값으로 넘겨줘야함.
    @PostMapping("/refreshToken")
    public ResponseEntity<?> refreshToken(@RequestBody TokenDTO tokenDTO) {
        log.info("RefreshToken 토큰 요청을 합니다.");

        String refreshToken = tokenDTO.getRefreshToken();

        try {
            // refreshToken을 검증하고 새로운 accessToken을 발급
            TokenDTO newTokenDto = userService.tokenReissue(refreshToken);

            log.info("새로운 access token 발급 생성 완료");
            return ResponseEntity.ok(newTokenDto);
        } catch (Exception e) {
            log.error("refresh token이 맞지 않음: " + e.getMessage());
            return ResponseEntity.status(403).body("refresh token이 맞지 않음");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String token) {
        log.info("logout token(successToken) : " + token);
        // "Bearer " 부분 제거 - 토큰 값이 "Bearer ${accessToken}" 이 방식으로 들어가기 때문
        String refreshToken = token.substring(7);
        log.info("refreshToken : " + refreshToken);
        // Refresh Token 삭제
        refreshTokenRepository.deleteByToken(refreshToken);
        // SecurityContextHolder 초기화
        SecurityContextHolder.clearContext();

        return ResponseEntity.ok("Logout successful");
    }
}
