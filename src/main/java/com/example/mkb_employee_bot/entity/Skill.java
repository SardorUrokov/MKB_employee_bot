package com.example.mkb_employee_bot.entity;

import com.example.mkb_employee_bot.entity.enums.SkillType;
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
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "skill_type")
    SkillType skillType;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Temporal(value = TemporalType.TIMESTAMP)
    Date createdAt = new Date();

    @Temporal(value = TemporalType.TIMESTAMP)
    Date updatedAt = new Date();
}