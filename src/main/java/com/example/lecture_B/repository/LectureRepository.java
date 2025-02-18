package com.example.lecture_B.repository;

import com.example.lecture_B.entity.Lecture;
import com.example.lecture_B.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


public interface LectureRepository extends JpaRepository<Lecture, Long> {
    // 특정 게시판에 속한 강의 조회
    List<Lecture> findByBoardId(Long boardId);

    Optional<User> findByUserId(Long userId);

}
