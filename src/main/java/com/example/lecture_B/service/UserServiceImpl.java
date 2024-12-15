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
    private final S3Service s3Service;

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
    public User signIn(String id, String pw) throws UseridException {

        Optional<User> user = userRepository.findByUserId(id);

        if (user.isPresent()) {
            if (user.get().isDel()) {
                log.info("삭제 예정인 아이디.");
                throw new UseridException("삭제된 아이디입니다.");
            } else if (passwordEncoder.matches(pw, user.get().getUserPw())) {
                return user.get();
            } else {
                log.info("비밀번호 불일치.");
                return null;
            }
        } else {
            log.info("존재하지 않는 아이디.");
            return null;
        }




//        if (user.isPresent()){
//            if (passwordEncoder.matches(pw, user.get().getUserPw())){
//                return user.get();
//            } else {
//                log.info("비밀번호 불일치.");
//                return null;
//            }
//        } else {
//            log.info("존재하지 않는 아이디.");
//            return null;
//        }


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
                throw new UseridException("닉네임이 이미 존재합니다.");
            }

            if (!user.getEmail().equals(modifyDTO.getEmail()) && userRepository.existsByEmail(modifyDTO.getEmail())){
                log.info("이미 있는 이메일");
                throw new UseridException("이메일이 이미 존재합니다.");
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
    public void userRemove(String userId) {
        log.info("회원을 삭제 하겠음.");
        log.info(userId);
        User user = userDetail(userId);
        user.changeDel(true);

        log.info(userId + "를 삭제하겠음.");

        userRepository.save(user);
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
    public UserDTO updateProfileImage(String userId, String imageUrl) {
        // 현재 회원의 정보를 조회.
        User user = userRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException("User not found"));

        // 이미지경로를 저장.
        user.setProfileImage(imageUrl);

        // 데이터베이스에저장

        userRepository.save(user);

        // dto를 통한 변환.
        return modelMapper.map(user, UserDTO.class);
    }


    @Override
    public TokenDTO tokenReissue(String refreshToken) {

        Map<String, Object> claims = jwtUtil.validateToken(refreshToken);

        String email = (String) claims.get("email");

        String newAccessToken = jwtUtil.generateToken(Map.of("email", email), 1); // 30분 유효

        return new TokenDTO(newAccessToken, refreshToken);
    }
}
