package com.example.lecture_B.security;

import com.example.lecture_B.entity.CustomUser;
import com.example.lecture_B.entity.User;
import com.example.lecture_B.repository.UserRepository;
import com.example.lecture_B.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
@Transactional
public class CustomUserDetailService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserService service;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {

        //DB에서 userID가 들어있는 행 찾기.
       Optional<User> result = userRepository.findByUserId(userId);

       //ID값이 없는경우 예외처리
        if (result.isEmpty()){
            throw new UsernameNotFoundException("유저가 존재하지 않음.");
        }

        User user = result.get();

        // ROLE 역할이 비어있는지 확인.
        if (user.getUserRole().isEmpty()){
            throw new UsernameNotFoundException("유저의 권한이 존재하지 않음.");
        }

        // 권한을 SimpleGrantedAuthority로 변환
        // 사용자의 역할을 SimpleGrantedAuthority로 변환하여 Spring Security의 권한 시스템과 통합
        List<SimpleGrantedAuthority> authorities;
        try {
            authorities = user.getUserRole().stream()
                    .peek(role -> log.info("유저한테 할당된 권한: ROLE_" +role))
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error converting roles to authorities", e);
            throw new UsernameNotFoundException("Cannot convert roles to authorities", e);
        }

        CustomUser customUser = new CustomUser(
                user.getUserId(),
                user.getUserPw(),
                user.getNickname(),
                user.getEmail(),
                user.isDel(),
                authorities
        );

        log.info("커스텀 유저 정보 : " + customUser);

        return customUser;

    }
}
