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

    @OneToMany(fetch = FetchType.EAGER)
    List<Education> educations;

    @OneToMany
    List<Skill> skills;

    @OneToMany
    Set<Attachment> attachments;

    boolean isDeleted = false;

    public Employee(String fullName, String dateOfBirth, String phoneNumber, String additionalNumber, String nationality, Integer age, Position position, List<Education> educations, List<Skill> skills, Set<Attachment> attachments) {
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.phoneNumber = phoneNumber;
        this.additionalNumber = additionalNumber;
        this.nationality = nationality;
        this.age = age;
        this.position = position;
        this.educations = educations;
        this.skills = skills;
        this.attachments = attachments;
    }
}