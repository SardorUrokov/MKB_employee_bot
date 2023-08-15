package com.example.mkb_employee_bot.repository;

import com.example.mkb_employee_bot.entiry.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Optional<Department> findByName(String name);

    @Query(value = "select name from Department")
    List<String> getDepartmentNames();
}