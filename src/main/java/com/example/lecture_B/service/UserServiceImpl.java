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




        User user = modelMapper.map(dto, User.class);



        user.setUserPw(passwordEncoder.encode(dto.getUserPw()));
        user.addRole(UserRole.USER);

        userRepository.save(user);

        return user;
    }
}
