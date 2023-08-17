package com.example.mkb_employee_bot.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.mkb_employee_bot.entiry.Department;
import com.example.mkb_employee_bot.repository.DepartmentRepository;
import com.example.mkb_employee_bot.service.interfaces.DepartmentService;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Override
    public Department createDepartment(String departmentName) {
        Department department = Department.builder()
                .name(departmentName)
                .build();
        return departmentRepository.save(department);
    }

    @Override
    public List<Department> getDepartmentList() {
        return departmentRepository.findAllByDeletedFalse();
    }

    @Override
    public Department updateDepartment(String departmentPreviousName, String departmentNewName) {
        final var department = departmentRepository.findByName(departmentPreviousName).get();
        department.setName(departmentNewName);
        return departmentRepository.save(department);
    }

    @Override
    public void deleteDepartment(String departmentName) {
        departmentRepository.updateDepartmentIsDeleted(departmentName);
    }
}