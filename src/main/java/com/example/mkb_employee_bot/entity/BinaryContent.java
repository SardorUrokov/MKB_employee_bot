package com.example.mkb_employee_bot.entity;

import jakarta.persistence.*;
import lombok.*;

import java.net.URL;

@Getter
@Setter
@EqualsAndHashCode(exclude = "id")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "binary_content")
public class BinaryContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private URL link;
    private byte[] fileAsArrayOfBytes;
}