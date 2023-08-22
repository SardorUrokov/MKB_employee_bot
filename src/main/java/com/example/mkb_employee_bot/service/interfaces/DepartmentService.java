package com.example.mkb_employee_bot.service.interfaces;

import com.example.mkb_employee_bot.entity.Department;

import java.util.List;

public interface DepartmentService {

    Department createDepartment(String departmentName);

    List<Department> getDepartmentList();

    Department updateDepartment(String departmentPreviousName, String departmentNewName);

    void deleteDepartment(String departmentName);
}