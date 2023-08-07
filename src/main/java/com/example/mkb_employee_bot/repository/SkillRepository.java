package com.example.mkb_employee_bot.repository;

import com.example.mkb_employee_bot.entiry.Skill;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SkillRepository extends JpaRepository<Skill, Long> {
}