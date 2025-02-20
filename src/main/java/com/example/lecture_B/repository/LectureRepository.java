package com.example.lecture_B.repository;

import com.example.lecture_B.entity.Lecture;
import com.example.lecture_B.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


public interface LectureRepository extends JpaRepository<Lecture, Long> {
    // 특정 게시판에 속한 강의 조회
    List<Lecture> findByBoardId(Long boardId);

    Optional<User> findByUserId(Long userId);

    Page<Lecture> findByBoardId(Long boardId, Pageable pageable);

    //페이징 처리와 닉네임 및 제목 검색
    @Query("SELECT l FROM Lecture l JOIN l.user u " +
            "WHERE l.title LIKE %:keyword% OR u.nickname LIKE %:keyword%")
    Page<Lecture> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

}
