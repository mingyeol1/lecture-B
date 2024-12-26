package com.example.lecture_B.repository;

import com.example.lecture_B.entity.Lecture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LectureRepository extends JpaRepository<Lecture, Long> {
    // 특정 게시판에 속한 강의 조회
    List<Lecture> findByBoardId(Long boardId);

}
