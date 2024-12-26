package com.example.lecture_B.dto;

import lombok.Data;

import java.util.List;

@Data
public class LectureRequestDTO {
    private String title;           //제목
    private String description;     //설명
    private String videoUrl;        //강의 영상.
    private List<String> imagesUrl;  // 필요한 경우
}
