package com.example.mkb_employee_bot.entiry;

import java.util.List;
import java.util.Set;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String fullName,
            dateOfBirth,
            phoneNumber,
            additionalNumber,
            nationality;
    Integer age;

    @ManyToOne
    Position position;

    @ManyToOne
    Education education;

    @OneToMany
    List<Skill> skills;

    @OneToMany
    Set<Attachment> attachments;
}