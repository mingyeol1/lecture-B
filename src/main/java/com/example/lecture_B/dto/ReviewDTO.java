package com.example.lecture_B.dto;

import lombok.Data;

@Data
public class ReviewDTO {
    private Long id;
    private Long lectureId;
    private Long userId;
    private String reviewContent;
    private int rating;
    private String userNickname;
}