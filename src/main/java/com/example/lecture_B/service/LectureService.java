package com.example.lecture_B.service;

import com.example.lecture_B.dto.BoardDTO;
import com.example.lecture_B.dto.LectureRequestDTO;
import com.example.lecture_B.dto.LectureResponseDTO;
import com.example.lecture_B.dto.LectureSearchDTO;
import com.example.lecture_B.entity.CustomUser;
import com.example.lecture_B.entity.Lecture;
import com.example.lecture_B.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface LectureService {

    //특정 게시판에 게시글 등록
    public Lecture createLecture(Long boardId, CustomUser customUser, String lectureString,
                                 List<MultipartFile> images, MultipartFile video) throws Exception;

    //게시글 내 강의 목록 조회
    public LectureResponseDTO getLectures(Long lectureId);

    //게시글 페이지네이션
    public Page<LectureResponseDTO> getLecturesByBoardId(Long boardId, int page, int size);

    //강의 수정
    public void modifyLectures(Long lectureId, String lectureString, List<MultipartFile> images,
                               MultipartFile video, CustomUser user) throws IOException;

    //강의 삭제
    public void deleteLecture(Long lectureId,Long userId);



    //페이지 검색
    public Page<LectureSearchDTO> searchLectures(String keyword, int page, int size);



}
