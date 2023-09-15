package com.example.mkb_employee_bot.service;

import java.util.List;
import java.time.Period;
import java.time.LocalDate;

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

    public List<Employee> employeeList() {
        return employeeRepository.findAll(Sort.by("full_name"));
    }

    public void deleteEmployee(Long id) {
        employeeRepository.updateEmployeeIsDeleted(id);
    }

    private String getEmployeeAge(String birthDate) {
        LocalDate parsedBirthDate = LocalDate.parse(birthDate);
        LocalDate currentDate = LocalDate.now();

        Period period = Period.between(parsedBirthDate, currentDate);
        return String.valueOf(period.getYears());
    }

    public Employee createEmployee(Employee creatingEmployee) {

        educationRepository.saveAll(creatingEmployee.getEducations());
        skillRepository.saveAll(creatingEmployee.getSkills());
        appPhotoRepository.saveAll(creatingEmployee.getAppPhotos());
        return employeeRepository.save(creatingEmployee);
    }

    public Employee updateEmployee(Employee updatingEmployee) {
        return employeeRepository.save(updatingEmployee);
    }
}