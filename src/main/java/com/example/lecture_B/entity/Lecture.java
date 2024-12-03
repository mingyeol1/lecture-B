package com.example.lecture_B.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class Lecture {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title; // 강의 제목
    private String description; // 강의 설명
    private String videoUrl; // 강의 영상 URL
    private double rating; // 평균 별점

    @ManyToOne
    @JoinColumn(name = "board_id", nullable = false) // 게시판 ID와 연관
    private Board board;

    @ManyToOne
    @JoinColumn(name = "uploader_id", nullable = false) // 업로더 ID와 연관
    private User uploader;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;
}
