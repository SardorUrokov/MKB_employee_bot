package com.example.mkb_employee_bot.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmployeePhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @OneToOne
    Employee employee;

    @OneToOne
    AppPhoto appPhoto;

    public EmployeePhoto(Employee employee, AppPhoto appPhoto) {
        this.employee = employee;
        this.appPhoto = appPhoto;
    }
}