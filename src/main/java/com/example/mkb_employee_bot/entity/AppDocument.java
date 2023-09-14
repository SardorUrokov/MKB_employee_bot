package com.example.mkb_employee_bot.entity;

import com.example.mkb_employee_bot.entity.enums.FileType;
import jakarta.persistence.*;
import lombok.*;

import java.net.URL;

@Getter
@Setter
@EqualsAndHashCode(exclude = "id")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "app_document")
public class AppDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String telegramFileId;
    private String docName;
    private String mimeType;

    @Enumerated(value = EnumType.STRING)
    private FileType fileType;

    private Long fileSize;
    private URL linkForDownloading;
    private byte[] fileAsArrayOfBytes;

    @OneToOne
    private BinaryContent binaryContent;
}