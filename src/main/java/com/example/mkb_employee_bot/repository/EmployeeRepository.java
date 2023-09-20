package com.example.mkb_employee_bot.repository;

import java.util.List;
import java.util.Optional;

import com.example.mkb_employee_bot.entity.*;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    List<Employee> findByFullNameIgnoreCaseContaining(String employeeName);

    Optional<Employee> findByFullName(String fullName);

    List<Employee> getEmployeesByPosition_Management_Department_Id(Long department_id);

    List<Employee> getEmployeesByPosition_Management_Id(Long management_id);

    List<Employee> getEmployeesByPosition_Id(Long position_id);

    @Transactional
    @Modifying
    @Query(value = "UPDATE Employee SET is_deleted = true, updated_at = CURRENT_TIMESTAMP where Employee.id = :id", nativeQuery = true)
    void updateEmployeeIsDeleted(Long id);

    @Query("SELECT e.appPhotos FROM Employee e WHERE e.id = :employeeId")
    List<AppPhoto> findAppPhotosByEmployeeId(Long employeeId);

    @Query("SELECT e.documents FROM Employee e WHERE e.id = :employeeId")
    List<AppDocument> findAppDocumentsByEmployeeId(Long employeeId);

}