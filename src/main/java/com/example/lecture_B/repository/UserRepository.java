package com.example.lecture_B.repository;

import com.example.lecture_B.dto.TokenDTO;
import com.example.lecture_B.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {


    // 유저의 Id값을 찾는 컬럼 만들기.
   Optional<User> findByUserId(String userId);

    //이미 유저 id가 존재할 경우.
    boolean existsByUserId(String userId);
    //이미 email이 가입되어 있는경우
    boolean existsByEmail(String email);
    //이미 닉네임이 가입되어 있는경우
    boolean existsByNickname(String nickname);


}
