package com.example.lecture_B.config;

import com.example.lecture_B.entity.CustomUser;
import com.example.lecture_B.repository.RefreshTokenRepository;
import com.example.lecture_B.security.CustomUserDetailService;
import com.example.lecture_B.security.filter.LoginFilter;
import com.example.lecture_B.security.filter.RefreshTokenFilter;
import com.example.lecture_B.security.filter.TokenCheckFilter;
import com.example.lecture_B.security.handler.UserLoginSuccessHandler;
import com.example.lecture_B.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@Log4j2
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class CustomSecurityConfig {

    private final CustomUserDetailService customUserDetailService;
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        log.info("------------------------- web configure ------------------------------");

        // 정적 리소스 필터링 제외
        return (web) -> web.ignoring()
                .requestMatchers(
                        PathRequest.toStaticResources().atCommonLocations()
                );
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // CSRF 보호 비활성화 (REST API에서는 불필요)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/signIn", "/api/auth/signUp").permitAll()  // 로그인과 회원가입은 인증 없이 접근 가능
                        .requestMatchers("/api/boards/**").hasRole("USER")  // 게시판 관련 API는 USER 권한 필요
                        .anyRequest().authenticated()  // 그 외 모든 요청은 인증 필요
                )
                .cors(cors -> cors.configurationSource(corsConfigurationSource())); // CORS 설정 적용

        // 세션을 사용하지 않는 STATELESS 설정 (JWT 기반 인증)
        http.sessionManagement((session) -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // AuthenticationManager 설정
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);

        authenticationManagerBuilder
                .userDetailsService(customUserDetailService)  // 사용자 정보를 가져오는 서비스 설정
                .passwordEncoder(passwordEncoder);  // 비밀번호 인코더 설정

        // AuthenticationManager 생성
        AuthenticationManager authenticationManager = authenticationManagerBuilder.build();

        // 인증 매니저 등록
        http.authenticationManager(authenticationManager);

        // LoginFilter 설정 - 로그인 요청 처리
        LoginFilter loginFilter = new LoginFilter("/api/auth/signIn");
        loginFilter.setAuthenticationManager(authenticationManager);
        loginFilter.setAuthenticationSuccessHandler(new UserLoginSuccessHandler(jwtUtil));
        http.addFilterBefore(loginFilter, UsernamePasswordAuthenticationFilter.class);

        // TokenCheckFilter 설정 - JWT 토큰 검증
        http.addFilterBefore(
                tokenCheckFilter(jwtUtil, customUserDetailService),  // JWT 검증 필터
                UsernamePasswordAuthenticationFilter.class
        );

        // RefreshTokenFilter 설정 - 토큰 갱신 처리
        http.addFilterBefore(
                new RefreshTokenFilter("/api/auth/refreshToken", jwtUtil, refreshTokenRepository),  // 토큰 갱신 엔드포인트 설정
                TokenCheckFilter.class
        );

        return http.build();
    }

    // CORS 필터 Bean 설정
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOriginPatterns(Arrays.asList("*")); // 모든 출처 허용 // 추후 front만 허용으로 바꿀예정.
        corsConfiguration.setAllowedMethods(Arrays.asList("HEAD", "GET", "POST", "PUT", "DELETE")); // 허용할 HTTP 메서드
        corsConfiguration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type")); // 허용할 헤더
        corsConfiguration.setAllowCredentials(true); // 인증 정보 허용 (쿠키 등)
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration); // 모든 경로에 적용
        return source;
    }

    private TokenCheckFilter tokenCheckFilter(JwtUtil jwtUtil, CustomUserDetailService customUserDetailService) {
        return new TokenCheckFilter(jwtUtil, customUserDetailService);
    }

    // refreshTokenFilter 설정
    @Bean
    public FilterRegistrationBean<RefreshTokenFilter> refreshTokenFilter() {
        FilterRegistrationBean<RefreshTokenFilter> filterRegistrationBean = new FilterRegistrationBean<>();

        filterRegistrationBean.setFilter(new RefreshTokenFilter("/refreshToken", jwtUtil, refreshTokenRepository));
        // 'api/*' 패턴의 모든 요청에 refreshToken Filter 적용
        filterRegistrationBean.addUrlPatterns("/api/*");

        return filterRegistrationBean;
    }
}
