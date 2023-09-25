package com.example.mkb_employee_bot.repository;

import java.util.List;
import com.example.mkb_employee_bot.entity.Education;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

public interface EducationRepository extends JpaRepository<Education, Long> {

    @Query(value = "SELECT ed FROM Employee e JOIN e.educations ed WHERE e.id = :employeeId and e.isDeleted = false")
    List<Education> findEmployeeEducations(@Param("employeeId") Long employeeId);

    @Transactional
    @Modifying
    @Query(value = "UPDATE Education SET is_deleted = true, updated_at = CURRENT_TIMESTAMP where Education.id = :eduId", nativeQuery = true)
    void updateEducationIsDeleted(Long eduId);
}