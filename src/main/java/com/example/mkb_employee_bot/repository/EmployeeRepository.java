package com.example.mkb_employee_bot.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.example.mkb_employee_bot.entity.Department;
import com.example.mkb_employee_bot.entity.Employee;
import com.example.mkb_employee_bot.entity.Skill;
import com.example.mkb_employee_bot.entity.enums.SkillType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

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

    List<Employee> getEmployeesByPosition_Management_Id(Long management_id);

    List<Employee> getEmployeesByPosition_Id(Long position_id);

    @Transactional
    @Modifying
    @Query(value = "UPDATE Employee SET is_deleted = true, updated_at = CURRENT_TIMESTAMP where Employee.id = :id", nativeQuery = true)
    void updateEmployeeIsDeleted(Long id);

//    List<Employee> findByDateOfBirthAndDeletedFalse(String dateOfBirth);
//
//    List<Employee> findByPosition_Management_Department_Id(Long department_id);
}