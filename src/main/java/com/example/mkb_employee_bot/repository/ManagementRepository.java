package com.example.mkb_employee_bot.repository;

import com.example.mkb_employee_bot.entiry.Management;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ManagementRepository extends JpaRepository<Management, Long> {

    @Query(value = "select name from Management ")
    List<String> getManagementNames();
}
