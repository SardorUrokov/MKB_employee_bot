package com.example.mkb_employee_bot.repository;

import java.util.Set;
import java.util.List;
import java.util.Optional;

import com.example.mkb_employee_bot.entity.*;
import com.example.mkb_employee_bot.entity.enums.SkillType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    List<Employee> findByFullNameIgnoreCaseContaining(String employeeName);

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
    @Query(value = "UPDATE Employee set full_name = :fullName, updated_at = CURRENT_TIMESTAMP where Employee.id = :id"
            , nativeQuery = true
    )
    Optional<Employee> updateEmployeeFull_name(@Param("fullName")String full_name, Long id);

    @Transactional
    @Modifying
    @Query(value = "UPDATE Employee set dateOfBirth = :birthDate, updated_at = CURRENT_TIMESTAMP where Employee.id = :id"
            , nativeQuery = true
    )
    Optional<Employee> updateEmployeeBirthDate(@Param("birthDate")String dateOfBirth, Long id);

    @Transactional
    @Modifying
    @Query(value = "UPDATE Employee set phone_number = :phoneNumber, updated_at = CURRENT_TIMESTAMP where Employee.id = :id"
            , nativeQuery = true
    )
    Optional<Employee> updateEmployeePhoneNumber(@Param("phoneNumber")String number, Long id);

    @Transactional
    @Modifying
    @Query(value = "UPDATE Employee set nationality = :employeeNationality, updated_at = CURRENT_TIMESTAMP where Employee.id = :id"
            , nativeQuery = true
    )
    Optional<Employee> updateEmployeeNationality(String employeeNationality, Long id);

    @Transactional
    @Modifying
    @Query(value = "UPDATE Employee set age = :newAge, updated_at = CURRENT_TIMESTAMP where Employee.id = :id"
            , nativeQuery = true
    )
    Optional<Employee> updateEmployeeAge(Integer newAge, Long id);

    @Transactional
    @Modifying
    @Query(value = "UPDATE Employee SET position = :newPosition, updated_at = CURRENT_TIMESTAMP WHERE id = :id"
            , nativeQuery = true
    )
    void updateEmployeePosition(@Param("newPosition") Position newPosition, @Param("id") Long id);

    @Transactional
    @Modifying
    @Query(value = "UPDATE Employee set educations = :newEducations, updated_at = CURRENT_TIMESTAMP where Employee.id = :id"
            , nativeQuery = true
    )
    Optional<Employee> updateEmployeeEducations(@Param("newEducations") List<Education> newEducations, @Param("id") Long id);

    @Transactional
    @Modifying
    @Query(value = "UPDATE Employee SET skills = :newSkills, updated_at = CURRENT_TIMESTAMP WHERE id = :id"
            , nativeQuery = true
    )
    void updateEmployeeSkills(@Param("newSkills") List<Skill> newSkills, @Param("id") Long id);


    @Transactional
    @Modifying
    @Query(value = "UPDATE Employee set attachments = :newAttachments, updated_at = CURRENT_TIMESTAMP where Employee.id = :id"
            , nativeQuery = true
    )
    Optional<Employee> updateEmployeeAttachments(List<Attachment> newAttachments, Long id);

    @Transactional
    @Modifying
    @Query(value = "UPDATE Employee SET is_deleted = true, updated_at = CURRENT_TIMESTAMP where Employee.id = :id", nativeQuery = true)
    void updateEmployeeIsDeleted(Long id);

    @Query("SELECT e.appPhotos FROM Employee e WHERE e.id = :employeeId")
    List<AppPhoto> findAppPhotosByEmployeeId(Long employeeId);

    @Query("SELECT e.documents FROM Employee e WHERE e.id = :employeeId")
    List<AppDocument> findAppDocumentsByEmployeeId(Long employeeId);
}