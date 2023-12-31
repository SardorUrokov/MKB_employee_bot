package com.example.mkb_employee_bot.service;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.mkb_employee_bot.entity.Management;
import com.example.mkb_employee_bot.entity.dto.ManagementDTO;
import com.example.mkb_employee_bot.repository.DepartmentRepository;
import com.example.mkb_employee_bot.repository.ManagementRepository;
import com.example.mkb_employee_bot.service.interfaces.ManagementService;

@Service
@RequiredArgsConstructor
public class ManagementServiceImpl implements ManagementService {

    private final ManagementRepository managementRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    public Management createManagement(ManagementDTO managementDTO) {

        final var department = departmentRepository
                .findById(managementDTO.getDepartmentId())
                .orElseThrow();

        Management management = Management.builder()
                .name(managementDTO.getName())
                .department(department)
                .build();

        return managementRepository.save(management);
    }

    @Override
    public List<Management> getManagementList() {
        return managementRepository.findAll();
    }

    @Override
    public Management updateManagement(Long id, ManagementDTO managementDTO) {

        final var department = departmentRepository
                .findById(managementDTO.getDepartmentId())
                .orElseThrow();

        final var management = managementRepository
                .findById(id)
                .orElseThrow();

        management.setName(managementDTO.getName());
        management.setDepartment(department);

        return managementRepository.save(management);
    }

    @Override
    public void deleteManagement(String managementName) {
        managementRepository.updateManagementIsDeleted(managementName);
    }
}