package com.example.mkb_employee_bot.entiry;

import com.example.mkb_employee_bot.entiry.enums.Language;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import com.example.mkb_employee_bot.entiry.enums.Role;

import java.util.Date;

@Data
@Builder
@Entity(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String fullName,
            userName,
            phoneNumber;
    Long userChatId;

    @Enumerated(EnumType.STRING)
    Language language;

    @Temporal(value = TemporalType.TIMESTAMP)
    Date createdAt = new Date();

    @Temporal(value = TemporalType.TIMESTAMP)
    Date updatedAt = new Date();

    @Enumerated(EnumType.STRING)
    Role role = Role.USER;
}