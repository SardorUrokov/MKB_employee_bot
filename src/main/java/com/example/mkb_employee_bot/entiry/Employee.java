package com.example.mkb_employee_bot.entiry;

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

    Integer age;
    Long userChatId;

    String fullName,
            dateOfBirth,
            phoneNumber,
            additionalNumber,
            nationality;

    @ManyToOne
    Position position;

    @ManyToOne
    Education education;

    @OneToMany
    Set<Attachment> attachments;

    @OneToMany
    Set<Skill> skills;
}