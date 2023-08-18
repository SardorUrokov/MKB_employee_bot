package com.example.mkb_employee_bot.service.interfaces;

import java.util.List;

import com.example.mkb_employee_bot.entiry.Management;
import com.example.mkb_employee_bot.entiry.dto.ManagementDTO;

public interface ManagementService {

    Management createManagement(ManagementDTO managementDTO);

    List<Management> getManagementList();

    Management updateManagement(Long id, ManagementDTO managementDTO);

    void deleteManagement(String managementName);
}