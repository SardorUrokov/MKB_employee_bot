package com.example.mkb_employee_bot.repository;

import com.example.mkb_employee_bot.entiry.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PositionRepository extends JpaRepository<Position, Long> {
    @Query(value = "select name from Position", nativeQuery = true)
    List<String> getPositionNames();
}