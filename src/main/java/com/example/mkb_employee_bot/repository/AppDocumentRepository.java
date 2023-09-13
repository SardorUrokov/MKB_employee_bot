package com.example.mkb_employee_bot.repository;

import com.example.mkb_employee_bot.entity.AppDocument;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppDocumentRepository extends JpaRepository<AppDocument, Long> {
}
