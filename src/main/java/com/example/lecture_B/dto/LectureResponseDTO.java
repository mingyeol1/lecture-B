package com.example.lecture_B.dto;

import lombok.Data;

@Data
public class LectureResponseDTO {
    private Long id; // 강의 ID
    private String title; // 강의 제목
    private String description; // 강의 설명
    private String videoUrl; // 강의 영상 URL
    private double rating; // 평균 별점
    private String boardName; // 강의가 속한 게시판 이름
    private String uploaderNickname; // 강의를 업로드한 유저 닉네임
}
