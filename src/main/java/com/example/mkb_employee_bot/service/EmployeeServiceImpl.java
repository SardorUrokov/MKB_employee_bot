package com.example.mkb_employee_bot.service;

import java.util.List;
import java.time.Period;
import java.time.LocalDate;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.example.mkb_employee_bot.entity.Employee;
import com.example.mkb_employee_bot.repository.EmployeeRepository;
import com.example.mkb_employee_bot.service.interfaces.EmployeeService;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;

    public List<Employee> employeeList() {
        return employeeRepository.findAll(Sort.by("full_name"));
    }

    public void deleteEmployee(Long id) {
        employeeRepository.updateEmployeeIsDeleted(id);
    }

    private String getUserAge(String birthDate) {

        LocalDate parsedBirthDate = LocalDate.parse(birthDate);
        LocalDate currentDate = LocalDate.now();

        Period period = Period.between(parsedBirthDate, currentDate);
        return String.valueOf(period.getYears());
    }

    public Employee createEmployee(Employee creatingEmployee) {
        return employeeRepository.save(creatingEmployee);
    }

//    public List<Employee> getEmployeesWithBirthday(String today) {
//        return employeeRepository.findByDateOfBirthAndDeletedFalse(today);
//    }
//
//    public List<Employee> getColleaguesInSameDepartment(Employee employee) {
//        final var departmentId = employee.getPosition().getManagement().getDepartment().getId();
//        return employeeRepository.findByPosition_Management_Department_Id(departmentId);
//    }
}