package com.example.mkb_employee_bot.service.interfaces;

import java.util.List;

import com.example.mkb_employee_bot.entity.Management;
import com.example.mkb_employee_bot.entity.dto.ManagementDTO;

public interface ManagementService {

    Management createManagement(ManagementDTO managementDTO);

    List<Management> getManagementList();

    Management updateManagement(Long id, ManagementDTO managementDTO);

    void deleteManagement(String managementName);
}