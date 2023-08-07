package com.example.mkb_employee_bot.repository;

import com.example.mkb_employee_bot.entiry.Education;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EducationRepository extends JpaRepository<Education, Long> {
}