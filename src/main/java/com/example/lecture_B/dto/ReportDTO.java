package com.example.lecture_B.dto;

import lombok.Data;

@Data
public class ReportDTO {
    private Long id;
    private Long reportedUserId;
    private Long reporterId;
    private String reportReason;
    private String reportType;
    private Long targetId;
}
