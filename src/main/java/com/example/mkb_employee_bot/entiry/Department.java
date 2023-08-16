package com.example.mkb_employee_bot.entiry;

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
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String name;

    boolean isDeleted = false;

    public Department(String name) {
        this.name = name;
    }

    @Temporal(value = TemporalType.TIMESTAMP)
    Date createdAt = new Date();

    @Temporal(value = TemporalType.TIMESTAMP)
    Date updatedAt = new Date();
}