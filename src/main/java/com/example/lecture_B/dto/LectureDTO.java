package com.example.lecture_B.dto;

import lombok.Data;

@Data
public class LectureDTO {
    private Long id;
    private String title;
    private String description;
    private String videoUrl;
    private double rating;
    private String boardName; // 강의가 속한 게시판 이름
    private String uploaderNickname; // 강의를 업로드한 유저 닉네임
}
