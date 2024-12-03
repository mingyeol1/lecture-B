package com.example.lecture_B.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "reported_user_id")
    private User reportedUser;

    @ManyToOne
    @JoinColumn(name = "reporter_id")
    private User reporter;

    private String reportReason;
    private String reportType; // 강의, 리뷰, 게시글 등
    private Long targetId;

    private LocalDateTime createdAt = LocalDateTime.now();
}
