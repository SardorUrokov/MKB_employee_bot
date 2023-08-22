package com.example.mkb_employee_bot.repository;

import java.util.Set;
import java.util.List;
import com.example.mkb_employee_bot.entity.Education;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EducationRepository extends JpaRepository<Education, Long> {

//    @Query("SELECT e FROM Education e WHERE e.id IN :eduIds")
    List<Education> findEducationByIdIn(Set<Long> eduIds);
}