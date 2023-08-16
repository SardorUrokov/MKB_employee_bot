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
public class Management {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String name;

    @ManyToOne
    Department department;

    boolean isDeleted = false;

    @Temporal(value = TemporalType.TIMESTAMP)
    Date createdAt = new Date();

    @Temporal(value = TemporalType.TIMESTAMP)
    Date updatedAt = new Date();

    public Management(String name, Department department) {
        this.name = name;
        this.department = department;
    }
}