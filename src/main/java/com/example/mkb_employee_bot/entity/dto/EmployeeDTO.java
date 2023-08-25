package com.example.mkb_employee_bot.entity.dto;

import com.example.mkb_employee_bot.entity.Education;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmployeeDTO {

    PersonalInfo personalInfo;
    EducationalInfo educationalInfo;
    SkillInfo skillInfo;
}