package com.example.lecture_B.repository;

import com.example.lecture_B.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board, Long> {
}
