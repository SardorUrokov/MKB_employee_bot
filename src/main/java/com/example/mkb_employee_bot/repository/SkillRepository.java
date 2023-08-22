package com.example.mkb_employee_bot.repository;

import com.example.mkb_employee_bot.entity.Skill;
import com.example.mkb_employee_bot.entity.enums.SkillType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;
import java.util.List;

public interface SkillRepository extends JpaRepository<Skill, Long> {

    @Query("SELECT s FROM Skill s WHERE s.id IN :skillIds AND s.skillType = :skillType")
    List<Skill> findSkillsByIdsAndSkillType(Set<Long> skillIds, SkillType skillType);
}
