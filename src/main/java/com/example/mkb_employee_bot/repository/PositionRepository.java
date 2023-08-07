package com.example.mkb_employee_bot.repository;

import com.example.mkb_employee_bot.entiry.Position;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PositionRepository extends JpaRepository<Position, Long> {
}