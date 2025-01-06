package com.example.lecture_B.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class BoardDTO {
    private Long id;
    private String name;
//    private String description;
    private List<LectureResponseDTO> lectures; // 게시판 내 강의 목록 // 게시판을 만들 때 이게 필요할까?
}
