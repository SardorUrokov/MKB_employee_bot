package com.example.mkb_employee_bot.entity.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PositionInfoDTO {

    Long positionId,
            managementId,
            departmentId;
}