package com.example.lecture_B.service;


import com.example.lecture_B.dto.SignUpDTO;
import com.example.lecture_B.dto.UserDTO;
import com.example.lecture_B.entity.User;
import com.example.lecture_B.entity.UserRole;
import com.example.lecture_B.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Log4j2
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User signUp(SignUpDTO dto) throws UseridException {


        //이미 가입된 아이디가 있을경우
        if (userRepository.existsByUserId(dto.getUserId())){

            log.info("이미 존재하는 아이디");
            throw  new UseridException();
        }
        //이미 가입된 이메일이 있을경우.
        if (userRepository.existsByEmail(dto.getEmail())){
            log.info("이미 존재하는 이메일");
            throw new UseridException();
        }
        //modelMapper를 이용해서 dto 변환.
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
}
