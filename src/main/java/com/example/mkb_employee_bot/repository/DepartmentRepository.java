package com.example.mkb_employee_bot.repository;

import com.example.mkb_employee_bot.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Optional<Department> findByName(String name);

    @Query(value = "select * from Department where is_deleted = false", nativeQuery = true)
    List<Department> findAllByDeletedFalse();

    @Query(value = "select name from Department")
    List<String> getDepartmentNames();

    @Transactional
    @Modifying
    @Query(value = "UPDATE Department SET is_deleted = true, updated_at = CURRENT_TIMESTAMP where Department.name = :departmentName", nativeQuery = true)
    void updateDepartmentIsDeleted (String departmentName);

//    @Transactional
//    @Modifying
//    @Query(value = "UPDATE Department d SET d.name = :newName, Department.updatedAt= CURRENT_TIMESTAMP  WHERE d.id = :departmentId", nativeQuery = true)
//    void updateDepartmentName(Long departmentId, String newName);
}