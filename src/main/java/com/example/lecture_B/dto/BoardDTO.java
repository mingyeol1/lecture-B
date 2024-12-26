package com.example.lecture_B.dto;

import lombok.Data;
import java.util.List;

@Data
public class BoardDTO {
    private Long id;
    private String name;
//    private String description;
    private List<LectureDTO> lectures; // 게시판 내 강의 목록
}
