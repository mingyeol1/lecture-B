package com.example.lecture_B.service;

import com.example.lecture_B.dto.LectureRequestDTO;
import com.example.lecture_B.dto.LectureResponseDTO;
import com.example.lecture_B.entity.CustomUser;
import com.example.lecture_B.entity.Lecture;
import com.example.lecture_B.entity.User;

public interface LectureService {

    //특정 게시판에 게시글 등록
    public Lecture createLecture(LectureRequestDTO dto, Long boardId, CustomUser user);


}
