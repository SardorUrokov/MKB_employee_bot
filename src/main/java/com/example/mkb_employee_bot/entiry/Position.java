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
public class Position {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String name;

    @ManyToOne
    Management management;

    boolean isDeleted = false;

    @Temporal(value = TemporalType.TIMESTAMP)
    Date createdAt = new Date();

    @Temporal(value = TemporalType.TIMESTAMP)
    Date updatedAt = new Date();

    public Position(String name, Management management) {
        this.name = name;
        this.management = management;
    }
}