package com.example.lecture_B.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    @ElementCollection
    @CollectionTable(name = "lecture_images", joinColumns = @JoinColumn(name = "lecture_id"))
    @Column(length = 1000000, name = "image_url")
    private List<String> imagesUrl = new ArrayList<>(); // 빈 리스트로 초기화

    private double rating; // 평균 별점

    @ManyToOne
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private LocalDateTime createdAt = LocalDateTime.now();  // 강의 업로드 날짜
    private LocalDateTime updatedAt;                        // 수정 날짜

    // 강의 수정
    public void update(String title, String description, List<String> imagesUrl,
                       String videoUrl, LocalDateTime updatedAt) {
        if (title != null) this.title = title;
        if (description != null) this.description = description;
        if (imagesUrl != null) this.imagesUrl = imagesUrl;
        if (videoUrl != null) this.videoUrl = videoUrl;
        this.updatedAt = updatedAt;
    }
}
