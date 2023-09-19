package com.example.mkb_employee_bot.service;

import com.example.mkb_employee_bot.entity.Attachment;
import com.example.mkb_employee_bot.entity.Employee;
import com.example.mkb_employee_bot.repository.AttachmentRepository;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Document;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private static final MinioClient minioClient = MinioClient.builder()
            .endpoint("http://localhost:9000")
            .credentials("minioadmin", "minioadmin")
            .build();
    private static final String BUCKET_NAME = "my-bucket";

//    private File downloadFile(FileType file) throws TelegramApiException {
//        try {
//            org.telegram.telegrambots.meta.api.objects.File fileDetails = DatabasePopulatorUtils.execute(new GetFile().setFileId());
//            EmployeeBot employeeBot = new EmployeeBot();
//            String fileUrl = fileDetails.getFileUrl(employeeBot.getBotToken());
//            // Download the file from the Telegram server
//            // You can implement this part as shown in previous responses
//            // and return the saved File object
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

    public void createAttachment(Employee employee, Document userDoc) {

        Attachment attachment = new Attachment();
        try {
            byte[] fileBytes = Files.readAllBytes(Path.of(userDoc.getThumb().getFilePath()));
            attachment.setBytes(fileBytes);
            attachment.setName(employee.getFullName() + "_" + attachment.getFileType().name());
            attachment.setCreatedAt(new Date());
            attachment.setUpdatedAt(new Date());
        } catch (IOException e) {
            e.printStackTrace();
        }

        attachmentRepository.save(attachment);
    }

    public void saveAttachmentToMinIO(Attachment attachment) {
        try {
            String objectName = "attachments/" + attachment.getFilePath(); // Adjust the path as needed

            // Upload the attachment to MinIO
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(BUCKET_NAME)
                            .object(objectName)
                            .stream(new FileInputStream(attachment.getFilePath()), -1, PutObjectArgs.MIN_MULTIPART_SIZE)
                            .contentType("application/octet-stream") // Set the appropriate content type
                            .build()
            );
            // Save the MinIO object path to the attachment object
            attachment.setMinioObjectPath(objectName);
            // Save the updated attachment object to your database
            attachmentRepository.save(attachment);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}