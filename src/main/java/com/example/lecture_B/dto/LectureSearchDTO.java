package com.example.lecture_B.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class LectureSearchDTO {
    private Long id;
    private String title;
    private String description;
    private String videoUrl;
    private List<String> imagesUrl;
    private double rating;
    private String userNickname;
    private LocalDateTime createdAt;


}