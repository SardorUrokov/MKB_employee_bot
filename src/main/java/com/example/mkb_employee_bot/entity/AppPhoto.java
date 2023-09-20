package com.example.mkb_employee_bot.entity;

import com.example.mkb_employee_bot.entity.enums.FileType;
import jakarta.persistence.*;
import lombok.*;

import java.net.URL;
import java.util.Date;

@Getter
@Setter
@EqualsAndHashCode(exclude = "id")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "app_photo")
public class AppPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String telegramFileId;
    private URL linkForDownloading;
    private Integer fileSize;

    @Enumerated(value = EnumType.STRING)
    private FileType fileType;

    private byte[] fileAsArrayOfBytes;

    @Temporal(value = TemporalType.TIMESTAMP)
    Date createdAt = new Date();
}