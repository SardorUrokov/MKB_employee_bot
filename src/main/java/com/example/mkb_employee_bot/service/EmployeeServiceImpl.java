package com.example.mkb_employee_bot.service;

import java.util.ArrayList;
import java.util.List;
import java.time.Period;
import java.time.LocalDate;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.example.mkb_employee_bot.repository.*;
import com.example.mkb_employee_bot.entity.Employee;
import com.example.mkb_employee_bot.service.interfaces.EmployeeService;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final SkillRepository skillRepository;
    private final EmployeeRepository employeeRepository;
    private final EducationRepository educationRepository;
    private final AppPhotoRepository appPhotoRepository;
    private final AppDocumentRepository appDocumentRepository;

    public List<Employee> employeeList() {
        return employeeRepository.findAll();
    }

    public List<Employee> getDepartmentEmployeesByDepartmentId(Long departmentId) {
        return employeeRepository.getEmployeesByPosition_Management_Department_Id(departmentId);
    }

    public List<String> getDepartmentEmployeesPhoneNumbers(Long departmentId) {
        return employeeRepository.findPhoneNumbersByDepartmentId(departmentId);
    }

    private List<String> getDepartmentEmployeesNames(Long departmentId) {
        return getDepartmentEmployeesByDepartmentId(departmentId)
                .stream()
                .map(Employee::getFullName)
                .collect(Collectors.toList());
    }

    public List<Employee> getManagementEmployeesByManagementId(Long managementId) {
        return employeeRepository.getEmployeesByPosition_Management_Id(managementId);
    }

    private List<Employee> getPositionEmployeesByPositionId(Long position_id) {
        return employeeRepository.getEmployeesByPosition_Id(position_id);
    }

    public Integer getEmployeeAge(String birthDate) {
        LocalDate parsedBirthDate = LocalDate.parse(birthDate);
        LocalDate currentDate = LocalDate.now();
        Period period = Period.between(parsedBirthDate, currentDate);
        return period.getYears();
    }

    public Employee createEmployee(Employee creatingEmployee) {

        educationRepository.saveAll(creatingEmployee.getEducations());
        skillRepository.saveAll(creatingEmployee.getSkills());

        if (creatingEmployee.getAppPhotos() != null)
            appPhotoRepository.saveAll(creatingEmployee.getAppPhotos());
        if (creatingEmployee.getDocuments() != null)
            appDocumentRepository.saveAll(creatingEmployee.getDocuments());

        return employeeRepository.save(creatingEmployee);
    }

    public Employee updateEmployee(Employee updatingEmployee) {
        final var savedEmployee = employeeRepository.save(updatingEmployee);
        employeeRepository.updateEmployeeUpdatedAt(savedEmployee.getId());
        return savedEmployee;
    }

    public void deleteEmployee(Long id) {
        employeeRepository.updateEmployeeIsDeleted(id);
    }

    public List<Employee> getEmployeesWithBirthday() {
        List<Employee> birthdayEmployees = new ArrayList<>();

        for (Employee employee : employeeList()) {
            if (employee.isBirthdayToday())
                birthdayEmployees.add(employee);
        }
        return birthdayEmployees;
    }

    public void increaseEmployeeAge(Employee babyEmployee) {
        final var employeeNewAge = babyEmployee.getAge() + 1;
        final var employeeId = babyEmployee.getId();
        employeeRepository.updateEmployeeAgeById(employeeId, employeeNewAge);
    }
}