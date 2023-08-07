package com.example.mkb_employee_bot.entiry;

import com.example.mkb_employee_bot.entiry.enums.EduType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

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
            startedDate,
            endDate;

    @Enumerated(EnumType.STRING)
    EduType type;
}