package com.example.lecture_B.dto;

import lombok.Data;

import java.util.List;

@Data
public class BoardResponseDTO {
    private Long id;
    private String name;
    private List<LectureResponseDTO> lectures;
}
