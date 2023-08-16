package com.example.mkb_employee_bot.service.interfaces;

import com.example.mkb_employee_bot.entiry.Department;

import java.util.List;

public interface DepartmentService {

    Department createDepartment(String departmentName);

    List<Department> getDepartmentList();

    void updateDepartment(String departmentPreviousName, String departmentNewName);

    void deleteDepartment(String departmentName);
}