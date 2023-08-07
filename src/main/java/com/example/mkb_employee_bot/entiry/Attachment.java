package com.example.mkb_employee_bot.entiry;

import com.example.mkb_employee_bot.entiry.enums.AttachmentType;
import com.example.mkb_employee_bot.entiry.enums.FileType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    byte[] bytes;

    @Enumerated(EnumType.STRING)
    FileType fileType;

    @Enumerated(EnumType.STRING)
    AttachmentType attachmentType;
}