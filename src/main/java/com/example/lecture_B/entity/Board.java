package com.example.lecture_B.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Data
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // 게시판 이름 (예: "프로그래밍 강의")
//    private String description; // 게시판 설명 (예: "프로그래밍 강의를 모아놓은 게시판")

    //ManyToOne과 이름이 다르면 에러남.
    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL)
    private List<Lecture> lectures; // 게시판에 속한 강의 목록
}

