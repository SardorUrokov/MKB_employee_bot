package com.example.mkb_employee_bot.repository;

import java.util.List;
import java.util.Optional;

import com.example.mkb_employee_bot.entiry.Position;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PositionRepository extends JpaRepository<Position, Long> {

    @Query(value = "select name from Position", nativeQuery = true)
    List<String> getPositionNames();

    Optional<Position> findByName(String name);
}