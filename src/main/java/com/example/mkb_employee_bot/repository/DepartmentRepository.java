package com.example.mkb_employee_bot.repository;

import com.example.mkb_employee_bot.entiry.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    @Query(value = "select name from Department")
    List<String> getDepartmentNames();
}
