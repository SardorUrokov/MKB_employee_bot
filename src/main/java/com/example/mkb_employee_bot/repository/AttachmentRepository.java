package com.example.mkb_employee_bot.repository;

import com.example.mkb_employee_bot.entiry.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
}
