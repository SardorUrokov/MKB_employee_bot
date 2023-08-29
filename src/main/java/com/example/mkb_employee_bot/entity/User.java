package com.example.mkb_employee_bot.entity;

import com.example.mkb_employee_bot.entity.enums.Role;
import com.example.mkb_employee_bot.entity.enums.Stage;
import com.example.mkb_employee_bot.entity.enums.Language;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

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
            phoneNumber,
            step;

    Long userChatId;

    @Enumerated(EnumType.STRING)
    Stage stage;

    @Enumerated(EnumType.STRING)
    Language language;

    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    @Enumerated(EnumType.STRING)
    Role role = Role.USER;

    public User(String phoneNumber, Role role) {
        this.phoneNumber = phoneNumber;
        this.role = role;
    }
}