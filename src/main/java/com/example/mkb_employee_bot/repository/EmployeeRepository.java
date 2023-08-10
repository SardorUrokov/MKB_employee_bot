package com.example.mkb_employee_bot.repository;

import java.util.List;
import java.util.Optional;

import com.example.mkb_employee_bot.entiry.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    List<Employee> findByFullNameIgnoreCaseContaining(String employeeName);

    boolean existsByPhoneNumber(String phoneNumber);

    Optional<Employee> findByPhoneNumber(String phoneNumber);

}