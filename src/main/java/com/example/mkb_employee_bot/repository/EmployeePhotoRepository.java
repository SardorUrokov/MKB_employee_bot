package com.example.mkb_employee_bot.repository;

import com.example.mkb_employee_bot.entity.EmployeePhoto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployeePhotoRepository extends JpaRepository<EmployeePhoto, Long> {

    Optional<EmployeePhoto> findByEmployee_Id(Long employee_id);
}
