package com.example.lecture_B.security.filter;

import com.example.lecture_B.security.handler.UserLoginSuccessHandler;
import com.google.gson.Gson;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

@Log4j2
public class LoginFilter extends AbstractAuthenticationProcessingFilter {

    private final UserLoginSuccessHandler userLoginSuccessHandler;

    // 생성자는 defaultFilterProcessUrl을 받아 부모 클래스의 생성자를 호출합니다.
    // 이는 필터가 특정 URL 패턴에 대해 작동하도록 설정
    public LoginFilter(String defaultFilterProcessUrl, AuthenticationManager authenticationManager, UserLoginSuccessHandler userLoginSuccessHandler) {

        super(defaultFilterProcessUrl);
        setAuthenticationManager(authenticationManager);
        this.userLoginSuccessHandler = userLoginSuccessHandler;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {

        log.info("LoginFilter...............");

        // 메서드 인증 시도 요청이 GET 메서드인 경우 인증을 처리하지 않고 null 반환
        if(request.getMethod().equalsIgnoreCase("GET")) {   // .getMethod로 GET 방식은 처리하지 않음
            log.info("GET METHOD NOT SUPPORT");
            return null;
        }

        Map<String, String> jsonData = parseRequestJSON(request);

        log.info("jsonData : "+jsonData);

        // UsernamePasswordAuthenticationToken에 UserId와 비밀번호를 담음
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                jsonData.get("userId"),
                jsonData.get("userPw")
        );

        if (jsonData.get("userId") == null || jsonData.get("userId").isEmpty()) {
            throw new AuthenticationServiceException("userId is missing");
        }
        if (jsonData.get("userPw") == null || jsonData.get("userPw").isEmpty()) {
            throw new AuthenticationServiceException("Password is missing");
        }
        log.info("authenticationToken : " + authenticationToken.getPrincipal().toString());
        // 호출하여 인증을 시도,
        // UsernamePasswordAuthenticationToken에서 만든 인증 토큰을 AuthenticaionManager에 토큰을 위임함
        return getAuthenticationManager().authenticate(authenticationToken);
    }

    // 요청의 JSON 데이터를 파싱하여 Map<String, String> 형태로 반환
    private Map<String, String> parseRequestJSON(HttpServletRequest request) {
        try(Reader reader = new InputStreamReader(request.getInputStream())) {
            // GSON으로 JSON 데이터를 Map으로 변환
            Gson gson = new Gson();
            return gson.fromJson(reader, Map.class);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain chain, Authentication authResult) throws IOException, ServletException {
        log.info("Login successful for user: " + authResult.getName());

        // 인증 성공 후 UserLoginSuccessHandler를 호출하여 응답을 처리
        userLoginSuccessHandler.onAuthenticationSuccess(request, response, authResult);
    }
}


