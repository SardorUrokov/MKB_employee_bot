package com.example.mkb_employee_bot.repository;

import com.example.mkb_employee_bot.entity.EmployeePhoto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeePhotoRepository extends JpaRepository<EmployeePhoto, Long> {
}
