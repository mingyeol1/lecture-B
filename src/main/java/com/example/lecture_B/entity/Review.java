package com.example.lecture_B.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "lecture_id", nullable = false)
    private Lecture lecture;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String reviewContent;
    private int rating; // 별점 (1~5)

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;
}
