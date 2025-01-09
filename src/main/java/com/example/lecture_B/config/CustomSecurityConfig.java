package com.example.lecture_B.config;

import com.example.lecture_B.entity.CustomUser;
import com.example.lecture_B.repository.RefreshTokenRepository;
import com.example.lecture_B.security.CustomUserDetailService;
import com.example.lecture_B.security.filter.RefreshTokenFilter;
import com.example.lecture_B.security.filter.TokenCheckFilter;
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
                .csrf(csrf -> csrf.disable()) // CSRF 보호 비활성화
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // 모든 요청 허용
                )
                .cors(cors -> cors.configurationSource(corsConfigurationSource())); // CORS 설정


        //로그인시 세션데이터 생성x
        http.sessionManagement((session) -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // AuthenticationManager 설정
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);

        authenticationManagerBuilder
                .userDetailsService(customUserDetailService)
                .passwordEncoder(passwordEncoder);

        // Get AuthenticationManager
        AuthenticationManager authenticationManager = authenticationManagerBuilder.build();

        // 인증 매니저 등록...
        http.authenticationManager(authenticationManager);

        // /api로 시작하는 모든 경로는 TokenCheckFilter 동작
        http.addFilterBefore(
                tokenCheckFilter(jwtUtil, customUserDetailService),
                UsernamePasswordAuthenticationFilter.class
        );

        // refreshToken 호출 처리
        http.addFilterBefore(new RefreshTokenFilter("/refreshToken", jwtUtil, refreshTokenRepository), TokenCheckFilter.class);


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
