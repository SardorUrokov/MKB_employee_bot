package com.example.mkb_employee_bot.entiry.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ManagementDTO {
    Long departmentId;
    String name;
}