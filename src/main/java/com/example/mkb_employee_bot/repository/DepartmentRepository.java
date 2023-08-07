package com.example.mkb_employee_bot.repository;

import com.example.mkb_employee_bot.entiry.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
}
