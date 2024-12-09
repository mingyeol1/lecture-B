package com.example.lecture_B.service;


import com.example.lecture_B.dto.ModifyDTO;
import com.example.lecture_B.dto.SignUpDTO;
import com.example.lecture_B.dto.TokenDTO;
import com.example.lecture_B.dto.UserDTO;
import com.example.lecture_B.entity.User;
import com.example.lecture_B.entity.UserRole;
import com.example.lecture_B.repository.UserRepository;
import com.example.lecture_B.util.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Log4j2
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public User signUp(SignUpDTO dto) throws UseridException {

        if (userRepository.existsByUserId(dto.getUserId())) {
            throw new UseridException("아이디가 이미 존재합니다.");  // 아이디 중복
        }

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new UseridException("이메일이 이미 존재합니다.");  // 닉네임 중복
        }

        if (userRepository.existsByNickname(dto.getNickname())){
            throw new UseridException("닉네임이 이미 존재합니다.");    //이메일 중복
        }

        User user = modelMapper.map(dto, User.class);
        user.setUserPw(passwordEncoder.encode(dto.getUserPw()));
        user.addRole(UserRole.USER);

        userRepository.save(user);

        return user;
    }


    @Override
    public User signIn(String id, String pw) {

        Optional<User> user = userRepository.findByUserId(id);

        if (user.isPresent()){
            if (passwordEncoder.matches(pw, user.get().getUserPw())){
                return user.get();
            }else {
                log.info("비밀번호 불일치.");
                return null;
            }
        } else {
            log.info("존재하지 않는 아이디.");
            return null;
        }
    }


    @Override
    public void modify(ModifyDTO modifyDTO) throws UseridException {
        log.info("회원정보를 수정하겠음----------------------------------.");

        Optional<User> optionalMember = userRepository.findByUserId(modifyDTO.getUserId());
        if (optionalMember.isPresent()) {
            User user = optionalMember.get();

            // 현재 유저의 닉네임과 이메일을 제외하고 중복 검사
            // 소셜로그인시 수정하면 nullPointerException 때문에 member.getMnick() != null 를 넣어줌.
            if (user.getNickname() != null && !user.getNickname().equals(modifyDTO.getNickname()) && userRepository.existsByNickname(modifyDTO.getNickname())) {
                log.info("이미 있는 닉네임");
                throw new UseridException("이미 있는 닉네임");
            }

            if (!user.getEmail().equals(modifyDTO.getEmail()) && userRepository.existsByEmail(modifyDTO.getEmail())){
                log.info("이미 있는 이메일");
                throw new UseridException("이미 있는 이메일");
            }

            // 기존 비밀번호를 임시로 저장
            String existingPassword = user.getUserPw();

            // DTO의 데이터를 엔티티에 매핑
            modelMapper.map(modifyDTO, user);

            // 비밀번호가 비어 있거나 기존 비밀번호와 같으면 기존 비밀번호 유지
            if (modifyDTO.getUserPw() == null || modifyDTO.getUserPw().isEmpty() || passwordEncoder.matches(modifyDTO.getUserPw(), existingPassword)) {
                user.setUserPw(existingPassword);
            } else {
                // 새 비밀번호를 인코딩하여 저장
                user.setUserPw(passwordEncoder.encode(modifyDTO.getUserPw()));
            }

            log.info("회원정보를 수정했음 ->" + user);

            userRepository.save(user);
        } else {
            log.info("회원 정보를 찾을 수 없음: ID=" + modifyDTO.getUserId());
            throw new RuntimeException("회원정보가 없는데요?");
        }
    }






    @Override
    public User userDetail(String id) {
        Optional<User> result = userRepository.findByUserId(id);


        if (result.isPresent()){
            User user = result.get();
            log.info("user-----------------------------------------");
            log.info(user);
            return user;
        }else {
            throw new NoSuchElementException("비어있음 : " + id);
        }

    }

    @Override
    public TokenDTO tokenReissue(String refreshToken) {

        Map<String, Object> claims = jwtUtil.validateToken(refreshToken);

        String email = (String) claims.get("email");

        String newAccessToken = jwtUtil.generateToken(Map.of("email", email), 1); // 30분 유효

        return new TokenDTO(newAccessToken, refreshToken);
    }
}
