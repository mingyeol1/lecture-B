package com.example.lecture_B.controller;

import com.example.lecture_B.dto.SignUpDTO;
import com.example.lecture_B.entity.RefreshToken;
import com.example.lecture_B.entity.User;
import com.example.lecture_B.repository.RefreshTokenRepository;
import com.example.lecture_B.repository.UserRepository;
import com.example.lecture_B.security.CustomUserDetailService;
import com.example.lecture_B.util.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
@Transactional
public class AuthController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailService customUserDetailService;
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    @PostMapping("/signUp")
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

        try {
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
                claim.put("userId", user.getUserId());      //유저 ID
                claim.put("nickname", user.getNickname());  //유저 닉네임
//            claim.put("phoneNum", user.getPhoneNum());    // 전화번호는 넣었다 민감할거 같아 제거.
                claim.put("email", user.getEmail());        //이메일
                claim.put("roles", userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()));     //유저 권한.

                String accessToken = jwtUtil.generateToken(claim, 1);   //엑세스토큰 기한 1일 추후 시간을 줄일 예정.
                String refreshToken = jwtUtil.generateToken(claim, 30); //리프레시토큰 기한 30일 추후 시간을 줄일 예정.

                // 리프레시 토큰을 DB에 저장하기 위한 메서드  userID 값과 같이 저장.
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

                // 토큰 값 저장.
                Map<String, String> tokens = Map.of("accessToken", accessToken, "refreshToken", refreshToken);

                // ok()에 클라이언트에게 반환할 토큰을 포함
                // ResponseEntity나 @ResponseBody 어노테이션을 사용하면 스프링은 기본적으로 데이터를 JSON 형식으로 변환하여 클라이언트에게 응답함.
                // 결론은 클라이언트는 JSON 형식으로 데이터를 받게 됨ㅁ
                return ResponseEntity.ok(tokens);
            } else {
                Map<String, String> errorReponse = new HashMap<>();
                errorReponse.put("error", "아이디나 비밀번호가 맞지 않습니다.");

                // 401에러 발생
                return ResponseEntity.status(401).body(errorReponse);
            }
        }catch (UserService.UseridException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }


    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String token) {
        log.info("logout token(successToken) : " + token);


  // 굳이 로그아웃할 때 디비값을 건드릴 필요가 있을까 싶어 주석. 허나 회원 탈퇴시에는 값을 삭제하도록 수정.
        // "Bearer " 부분 제거 - 토큰 값이 "Bearer ${accessToken}" 이 방식으로 들어가기 때문
    //토큰값이 삭제가 안돼서 나중에 다시 수정.
//        String refreshToken = token.substring(7);
//        log.info("refreshToken : " + refreshToken);
//        // Refresh Token 삭제
//        refreshTokenRepository.deleteByToken(refreshToken);


        // SecurityContextHolder 초기화
        SecurityContextHolder.clearContext();

        return ResponseEntity.ok("Logout successful");
    }




}
