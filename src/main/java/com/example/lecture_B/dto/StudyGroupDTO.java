package com.example.lecture_B.dto;

import lombok.Data;

@Data
public class StudyGroupDTO {
    private Long id;
    private String name;
    private String description;
    private String location;
    private String leaderNickname;
}
