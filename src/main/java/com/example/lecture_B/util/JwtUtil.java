package com.example.lecture_B.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

// JWT를 검증하고 생성하는 클래스
@Log4j2
@Component
public class JwtUtil {

    @Value("${spring.jwt.secret}")
    private String key;

    // 토큰 생성 메서드 Map으로 사용자 정보 및 권한 등 데이터 맵 전달.
    public String generateToken(Map<String, Object> valueMap, int days) {

        log.info("generateKey....."+key);

        // 헤더 부분 0.12.5 버전에서 더이상 사용되지 않아 주석.
//        Map<String, Object> headers = new HashMap<>();
//        headers.put("typ","JWT");
//        headers.put("alg","HS256");

        // payload 부분
        Map<String, Object> payloads = new HashMap<>();
        payloads.putAll(valueMap);

        // 토큰 생성 시간 설정...
        int time = (60 * 24) * days;   // 시간설정 변경... 1day로 ...
//        int time = (1 * 1) * minutes;


//          0.12.5버전에서는 더이상 사용되지 않아 주석.
//        String jwtStr = Jwts.builder()
//                .setHeader(headers)
//                .setClaims(payloads)
//                .setIssuedAt(Date.from(ZonedDateTime.now().toInstant()))
//                .setExpiration(Date.from(ZonedDateTime.now().plusMinutes(time).toInstant()))    // 분 단위로 처리 나중에 plusDays()로 변경 해줘야 함
//                .signWith(SignatureAlgorithm.HS256, key.getBytes())
//                .compact();

        String jwtStr = Jwts.builder()
                .header()
                .add("typ", "JWT")  // 헤더부분 빌더에 직접 추가.
                .add("alg", "HS256")    // 헤더부분.
                .and()          //헤더 설정 완료
                .claims(payloads)
                .issuedAt(Date.from(ZonedDateTime.now().toInstant()))
                .expiration(Date.from(ZonedDateTime.now().plusMinutes(time).toInstant()))
                .signWith(Keys.hmacShaKeyFor(key.getBytes()), Jwts.SIG.HS256)
                .compact();

        return jwtStr;
    }

    // 토큰 검증 메서드
    // 주어진 토큰을 검증하고, 유효한 경우 클레임을 Map<String, Object> 형태로 반환
    public Map<String, Object> validateToken(String token) throws JwtException {
        try {

            // 0.12.5 버전에서는 더이상 사용되지 않아 주석.
//           Claims claims = Jwts.parser()
//                   .setSigningKey(key.getBytes()).build()  // 서명 검증을 위한 키 설정
//                   .parseSignedClaims(token)               // 토큰 파싱 및 클레임 추출
//                   .getBody();                             // 클레임 반환

            Claims claims = Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(key.getBytes())).build()
                    .parseSignedClaims(token)
                    .getPayload();
            return new HashMap<>(claims);
        } catch (ExpiredJwtException e) {
            log.error("토큰 유효시간이 만료되었습니다: " + e.getMessage());
            throw e;
        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 JWT 형식입니다: " + e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            log.error("토큰의 형식이나 구조가 올바르지 않습니다: " + e.getMessage());
            throw e;
        } catch (SignatureException e) {
            log.error("토큰의 서명이 올바르지 않습니다: " + e.getMessage());
            throw e;
        } catch (PrematureJwtException e) {
            log.error("토큰이 아직 유효하지 않습니다: " + e.getMessage());
            throw e;
        } catch (ClaimJwtException e) {
            log.error("클레임 값이 잘못됐습니다: " + e.getMessage());
            throw e;
        } catch (JwtException e) {
            log.error("토큰이 유효하지 않습니다: " + e.getMessage());
            throw e;
        }
    }
}