package com.example.mkb_employee_bot.entiry;

import com.example.mkb_employee_bot.entiry.enums.SkillType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

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
}