package com.example.mkb_employee_bot.repository;

import com.example.mkb_employee_bot.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
}
