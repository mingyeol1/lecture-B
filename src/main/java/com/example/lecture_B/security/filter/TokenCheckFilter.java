package com.example.lecture_B.security.filter;


import com.example.lecture_B.security.CustomUserDetailService;
import com.example.lecture_B.security.filter.excption.AccessTokenException;
import com.example.lecture_B.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

// JWT값 검증 필터
// JWTUtil의 validateToken() 기능 활용
@Log4j2
@RequiredArgsConstructor
public class TokenCheckFilter extends OncePerRequestFilter {    // OncePerRequestFilter는 하나의 요청에[ 대해서 한번씩 동작하는 필터

    private final JwtUtil jwtUtil;  // 의존성 주입으로 jwtUtil의 validateToken을 불러와야함
    // userDetailsService를 이용해서 JWT의 userId 값으로 사용자 정보를 얻어옴
    private final CustomUserDetailService customUserDetailService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        //토큰값이 없어도 들어갈 수 있는 페이지 //검색 API 요청은 인증 체크 제외
        if (path.equals("/api/auth/signUp") || path.equals("/api/auth/signIn")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (path.startsWith("/api/boards") || path.startsWith("/api/lectures")) {
            filterChain.doFilter(request, response);  // 토큰 없이 통과
            return;
        }

        if(!path.startsWith("/api")) {  // /api로 시작하지 않는 경우는 리턴
            filterChain.doFilter(request,response); // 다음 필터로 넘김
            return;
        }

        log.info("Token Check Filter......");
        log.info("JWTUtil : " + jwtUtil);

        // 경로가 /api로 시작하는 경우 validateAccessToken 메서드를 호출하여 JWT 토큰을 검증함
        try {
            Map<String, Object> payload = validateAccessToken(request);

            // userId를 추출 > jwt 클레임 데이터에 포함된 사용자 식별자.
            String userId = (String) payload.get("userId");
            log.info("userId : " + userId);

            //userId 기반으로 사용자 정보 가져옴.
            UserDetails userDetails = customUserDetailService.loadUserByUsername(userId);

            // 등록 사용자 인증 정보 생성
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails, userDetails.getPassword(), userDetails.getAuthorities()
                    );

            //Spring Security의 SecurityContext에 인증 정보를 저장하여 요청 처리 중 인증 상태를 유지
            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);

            //토큰이 유효하지 않은 경우 예외를 처리
        } catch (AccessTokenException accessTokenException) {
            accessTokenException.sendResponseError(response);
            log.error("accessToken error" + accessTokenException);
        }
    }

    // 클라이언트에서 보낸 Access Token 검증
    private Map<String, Object> validateAccessToken(HttpServletRequest request) throws AccessTokenException {

        // 클라이언트가 서버로 요청을 보낼 때 HTTP 요청 헤더에 포함된 AccessToken을 추출
        // "Authorization" 헤더를 사용하여 토큰을 전달한 토큰을 받아옴
        // Authorization: Bearer ${access_token} - 클라이언트에서 이 값으로 보내줘야 함
        String headerStr = request.getHeader("Authorization");
        log.info("headerStr의 length : " + headerStr.length());

        // Authorization 헤더가 없거나 길이가 8미만이면 UNACCEPT 예외를 던짐
        // 8미만으로 짜르는 이유는 headerStr이 "Bearer <access_token>" 이렇게 이루어져 있기 때문
        if(headerStr == null || headerStr.length() < 8) {
            throw new AccessTokenException(AccessTokenException.TOKEN_ERROR.UNACCEPT);
        }

        // Bearer 생략
        String tokenType = headerStr.substring(0, 6);
        String tokenStr = headerStr.substring(7);  //Bearer를 제외한 실제 토큰 값

        // 타입이 Bearer인지 확인하고 올바르지 않으면 BADTYPE 예외를 던짐
        if(tokenType.equalsIgnoreCase("Bearer") == false) {
            throw new AccessTokenException(AccessTokenException.TOKEN_ERROR.BADTYPE);
        }

        //토큰 유효성검사.
        //추출한 토큰 값을 JwtUtil.validateToken 메서드로 전달하여 토큰의 서명값 및 만료여부 등을 검증.
        try {
            Map<String, Object> values = jwtUtil.validateToken(tokenStr);

            return values;

            //값이 유효하지 않으면 예외처리.
        } catch (MalformedJwtException malformedJwtException) {
            log.info("MalformedJwtException-----------------------");
            throw new AccessTokenException(AccessTokenException.TOKEN_ERROR.MALFORM);
        } catch (SignatureException signatureException) {
            log.info("SignatureException---------------------------");
            throw new AccessTokenException(AccessTokenException.TOKEN_ERROR.BADSIGN);
        } catch (ExpiredJwtException expiredJwtException) {
            log.info("ExpiredJwtException--------------------------");
            throw new AccessTokenException(AccessTokenException.TOKEN_ERROR.EXPIRED);
        }
    }
}

