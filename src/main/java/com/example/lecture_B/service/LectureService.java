package com.example.lecture_B.service;

import com.example.lecture_B.dto.LectureRequestDTO;
import com.example.lecture_B.dto.LectureResponseDTO;
import com.example.lecture_B.entity.CustomUser;
import com.example.lecture_B.entity.Lecture;
import com.example.lecture_B.entity.User;

import java.util.List;

public interface LectureService {

    //특정 게시판에 게시글 등록
    public Lecture createLecture(LectureRequestDTO dto, Long boardId, CustomUser user);

    //게시판 내 강의 목록 조회
    public List<Lecture> getLectures(Long boardId);

    //강의 삭제
    public void deleteLecture(Long lectureId);


}