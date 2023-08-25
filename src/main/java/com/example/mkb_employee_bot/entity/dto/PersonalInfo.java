package com.example.mkb_employee_bot.entity.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PersonalInfo {

    String fullName,
            dateOfBirth,
            phoneNumber,
            additionalNumber,
            nationality;
    Integer age;
}