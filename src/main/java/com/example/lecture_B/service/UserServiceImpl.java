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
            throw new UseridException("이메일이 이미 존재합니다.");  // 이메일 중복
        }

        if (userRepository.existsByNickname(dto.getNickname())){
            throw new UseridException("닉네임이 이미 존재합니다.");    //닉네임 중복
        }


        // SignUpDTO와 User 클래스 매핑.
        // 서로 값이 다르면 매핑이 진행되지않고 같은 메서드가(getter, setter) 있어야함
        User user = modelMapper.map(dto, User.class);
        //패스워드 인코딩.
        user.setUserPw(passwordEncoder.encode(dto.getUserPw()));
        //USER권한 부여
        user.addRole(UserRole.USER);
        //저장.
        userRepository.save(user);

        return user;
    }


    @Override
    public User signIn(String id, String pw) throws UseridException {
        //유저가 없는지 있는지 확인. 유저의 프라이머리키가 아닌 id값으로 확인으로 넣었음.
        Optional<User> user = userRepository.findByUserId(id);

        //사용자 로그인
        if (user.isPresent()) {     //Optional 메서드로 값이 있는지 없는지 확인 왜 사용하느냐 null포인트 익셉션 방지.
            if (user.get().isDel()) {   //get().isDel이 ture면 if문 동작
                log.info("삭제 예정인 아이디.");
                throw new UseridException("삭제된 아이디입니다.");
            } else if (passwordEncoder.matches(pw, user.get().getUserPw())) {   //아니면 비밀번호 디코딩 후 사용자 정보 출력.
                return user.get();                                              //사용자 정보.
            } else {
                log.info("비밀번호 불일치.");
                throw new UseridException("아이디 및 비밀번호가 다릅니다.");
            }
        } else {
            log.info("존재하지 않는 아이디.");
            throw new UseridException("아이디 및 비밀번호가 다릅니다.");
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

        //계정이 있는지 확인.
        Optional<User> optionalUser = userRepository.findByUserId(modifyDTO.getUserId());
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            // 현재 유저의 닉네임과 이메일을 제외하고 중복 검사
            if (user.getNickname() != null && !user.getNickname().equals(modifyDTO.getNickname()) && userRepository.existsByNickname(modifyDTO.getNickname())) {
                log.info("이미 있는 닉네임");
                throw new UseridException("닉네임이 이미 존재합니다.");
            }

            if (!user.getEmail().equals(modifyDTO.getEmail()) && userRepository.existsByEmail(modifyDTO.getEmail())){
                log.info("이미 있는 이메일");
                throw new UseridException("이메일이 이미 존재합니다.");
            }

            // 기존 비밀번호를 임시로 저장
            String existingPw = user.getUserPw();

            // DTO의 데이터를 엔티티에 매핑
            modelMapper.map(modifyDTO, user);

            // 비밀번호가 비어 있거나 기존 비밀번호와 같으면 기존 비밀번호 유지
            if (modifyDTO.getUserPw() == null || modifyDTO.getUserPw().isEmpty() || passwordEncoder.matches(modifyDTO.getUserPw(), existingPw)) {
                user.setUserPw(existingPw);
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


    //유저의 상세 정보를 나타내는 메서드.
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

        String userId = (String) claims.get("userId");

        String newAccessToken = jwtUtil.generateToken(Map.of("userId", userId), 1); // 30분 유효

        return new TokenDTO(newAccessToken, refreshToken);
    }
}
