package com.example.mkb_employee_bot.entity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.util.Locale;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@ToString
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
    List<Education> educations = new ArrayList<>();

    @OneToMany(fetch = FetchType.EAGER)
    @ToString.Exclude
    List<Skill> skills = new ArrayList<>();

    @OneToMany(fetch = FetchType.EAGER)
    @ToString.Exclude
    List<AppPhoto> appPhotos = new ArrayList<>();

    @OneToMany(fetch = FetchType.EAGER)
    @ToString.Exclude
    List<AppDocument> documents = new ArrayList<>();

    boolean isDeleted = false;

    public boolean isBirthdayToday() {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);
        LocalDate employeeBirthday = LocalDate.parse(dateOfBirth, formatter);
        return today.getMonth() == employeeBirthday.getMonth() && today.getDayOfMonth() == employeeBirthday.getDayOfMonth();
    }

    @Temporal(value = TemporalType.TIMESTAMP)
    Date createdAt = new Date();

    @Temporal(value = TemporalType.TIMESTAMP)
    Date updatedAt = new Date();

    public Employee(String fullName, String dateOfBirth, String phoneNumber, String additionalNumber, String nationality, Integer age, Position position, List<Education> educations, List<Skill> skills, List<AppPhoto> appPhotos, List<AppDocument> documents) {
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.phoneNumber = phoneNumber;
        this.additionalNumber = additionalNumber;
        this.nationality = nationality;
        this.age = age;
        this.position = position;
        this.educations = educations;
        this.skills = skills;
        this.appPhotos = appPhotos;
        this.documents = documents;
    }
}