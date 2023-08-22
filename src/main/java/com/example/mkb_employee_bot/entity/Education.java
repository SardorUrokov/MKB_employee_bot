package com.example.mkb_employee_bot.entity;

import com.example.mkb_employee_bot.entity.enums.EduType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Education {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String name,
            educationField,
            startedDate,
            endDate;

    @Enumerated(EnumType.STRING)
    EduType type;

    @Temporal(value = TemporalType.TIMESTAMP)
    Date createdAt = new Date();

    @Temporal(value = TemporalType.TIMESTAMP)
    Date updatedAt = new Date();
}