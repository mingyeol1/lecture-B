package com.example.lecture_B.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Lecture {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255, nullable = false)
    private String title; // 강의 제목
    @Column(length = 1000000, nullable = false)
    private String description; // 강의 설명

    private String videoUrl; // 강의 영상 URL
    private List<String> imagesUrl; //강의 이미지 URL
    private double rating; // 평균 별점

    //OneToMany와 다르면 에러남.
    @ManyToOne
    @JoinColumn(name = "board_id", nullable = false) // 게시판 ID와 연관
    private Board board;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false) // 업로더 ID와 연관
    private User user;

    private LocalDateTime createdAt = LocalDateTime.now();  //강의 업로드 날짜
    private LocalDateTime updatedAt;                        //수정 날짜.
}
