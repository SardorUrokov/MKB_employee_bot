package com.example.mkb_employee_bot.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.example.mkb_employee_bot.entiry.Employee;
import com.example.mkb_employee_bot.entiry.Skill;
import com.example.mkb_employee_bot.entiry.enums.SkillType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    List<Employee> findByFullNameIgnoreCaseContaining(String employeeName);

    boolean existsByPhoneNumber(String phoneNumber);

    Optional<Employee> findByFullName(String fullName);

    @Query(value = "select employee_skills from Employee e WHERE e.id = :employeeId AND e.skills.skill_type = :skillType", nativeQuery = true)
    List<Skill> findSkillsByEmployeeIdAndSkill(Long employeeId, SkillType skillType);

    @Query(value = "select skills_id from employee_skills where employee_id = :employeeId", nativeQuery = true)
    Set<Long> getEmployeeSkillsIds(Long employeeId);

    @Query(value = "select educations_id from employee_educations where employee_id = :employeeId", nativeQuery = true)
    Set<Long> getEmployeeEducationsIds(Long employeeId);

    List<Employee> getEmployeesByPosition_Management_Department_Id(Long department_id);
}