package com.example.mkb_employee_bot.repository;

import java.util.List;
import java.util.Optional;

import com.example.mkb_employee_bot.entity.Management;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface ManagementRepository extends JpaRepository<Management, Long> {

    @Query(value = "select name from Management where is_deleted = false", nativeQuery = true)
    List<String> getManagementNames();

    @Query(value = "select m.name from Management m where m.department_id = :department_id", nativeQuery = true)
    List<String> getManagementNamesByDepartmentId(@Param("department_id") Long departmentId);

    Optional<Management> findByName(String name);

    @Transactional
    @Modifying
    @Query(value = "UPDATE Management SET is_deleted = true, updated_at = CURRENT_TIMESTAMP where Management.name = :name", nativeQuery = true)
    void updateManagementIsDeleted(String name);

}