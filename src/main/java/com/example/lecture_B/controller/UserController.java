package com.example.lecture_B.controller;

import com.example.lecture_B.dto.SignUpDTO;
import com.example.lecture_B.dto.TokenDTO;
import com.example.lecture_B.dto.UserDTO;
import com.example.lecture_B.entity.RefreshToken;
import com.example.lecture_B.entity.User;
import com.example.lecture_B.repository.RefreshTokenRepository;
import com.example.lecture_B.repository.UserRepository;
import com.example.lecture_B.security.CustomUserDetailService;
import com.example.lecture_B.security.filter.excption.UserNotFoundException;
import com.example.lecture_B.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
import java.util.Optional;
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
    private final RefreshTokenRepository refreshTokenRepository;

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


    @RequestMapping(value = "/signIn", method = RequestMethod.POST)
    public ResponseEntity<?> signin(@RequestBody SignUpDTO dto) {
        log.info(dto.getUserId());

        // AuthService를 사용하여 사용자의 인증을 시도
        User user = userService.signIn(dto.getUserId(), dto.getUserPw());

        // 사용자가 존재하는 경우
        if (user != null) {
            // 사용자의 이메일을 기반으로 UserDetails를 가져옴
            UserDetails userDetails = customUserDetailService.loadUserByUsername(dto.getUserId());

            // 토큰 생성
            // UsernamePasswordAuthenticationToken, UserId=Principal, Password=Credential 역할을 함
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(), userDetails.getAuthorities());

            // SecurityContextHolder에 인증을 설정
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            // JWT Payload에 userId, userName, email, roles 값을 실어서 보냄
            Map<String, Object> claim = new HashMap<>();
            claim.put("userId", user.getUserId());
            claim.put("nickname", user.getNickname());
            claim.put("phoneNum", user.getPhoneNum());
            claim.put("email", user.getEmail());
            claim.put("roles", userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList()));

            String accessToken = jwtUtil.generateToken(claim, 1);
            String refreshToken = jwtUtil.generateToken(claim, 30);

            Optional<RefreshToken> existingRefreshToken = refreshTokenRepository.findByUserId(userDetails.getUsername());

            if (existingRefreshToken.isPresent()) {
                RefreshToken firstRefreshToken = existingRefreshToken.get();
                firstRefreshToken.setToken(refreshToken);
                refreshTokenRepository.save(firstRefreshToken);
            } else {
                RefreshToken newRefreshToken = new RefreshToken();
                newRefreshToken.setToken(refreshToken);
                newRefreshToken.setUserId(userDetails.getUsername());
                refreshTokenRepository.save(newRefreshToken);
            }

            Map<String, String> tokens = Map.of("accessToken", accessToken, "refreshToken", refreshToken);

            // ok()에 클라이언트에게 반환할 토큰을 포함
            // ResponseEntity나 @ResponseBody 어노테이션을 사용하면 스프링은 기본적으로 데이터를 JSON 형식으로 변환하여 클라이언트에게 응답함.
            // 결론은 클라이언트는 JSON 형식으로 데이터를 받게 됨ㅁ
            return ResponseEntity.ok(tokens);
        }else {
            Map<String, String> errorReponse = new HashMap<>();
            errorReponse.put("error", "아이디나 비밀번호가 맞지 않습니다.");

            // 401에러 발생
            return ResponseEntity.status(401).body(errorReponse);
        }
    }


    @PreAuthorize("hasRole('USER')")
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
