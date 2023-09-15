package com.example.mkb_employee_bot.service;

import com.example.mkb_employee_bot.component.CryptoTool;
import com.example.mkb_employee_bot.entity.*;
import com.example.mkb_employee_bot.entity.enums.FileType;
import com.example.mkb_employee_bot.entity.enums.LinkType;
import com.example.mkb_employee_bot.exceptions.UploadFileException;
import com.example.mkb_employee_bot.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaDocument;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
@Service
public class FileService {

    @Value("${token}")
    private String token;

    @Value("${service.file_info.uri}")
    private String fileInfoUri;

    @Value("${service.file_storage.uri}")
    private String fileStorageUri;

    private static CryptoTool cryptoTool;
    private final ButtonService buttonService;
    private final AppPhotoRepository appPhotoRepository;
    private final AppDocumentRepository appDocumentRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeePhotoRepository employeePhotoRepository;
    private final BinaryContentRepository binaryContentRepository;

    public CompletableFuture<SendMessage> processDoc(FileType fileType, Update update, Employee employee) {
        return CompletableFuture.supplyAsync(() -> {

                    long chatId = update.getMessage().getChatId();
                    var telegramDoc = update.getMessage().getDocument();
                    var fileId = telegramDoc.getFileId();
                    var response = getFilePath(fileId);

                    if (response.getStatusCode() == HttpStatus.OK) {
                        var persistentBinaryContent = getPersistentBinaryContent(response);
                        var transientAppDoc = buildTransientAppDoc(fileType, telegramDoc, persistentBinaryContent);
                        final var appDocument = appDocumentRepository.save(transientAppDoc);
                        employee.setDocuments(List.of(appDocument));
                        employeeRepository.save(employee);

                        final var messageCompletableFuture = buttonService.completeAddingEmployeeInfo(update, employee);
                        final var sendMessage = messageCompletableFuture.join();
                        final var text = sendMessage.getText();
                        final var replyMarkup = sendMessage.getReplyMarkup();

                        return SendMessage.builder()
                                .replyMarkup(replyMarkup)
                                .text(text)
                                .chatId(chatId)
                                .build();
                    } else {
                        throw new UploadFileException("Bad response from telegram service: " + response);
                    }
                }
        );
    }

    public CompletableFuture<SendMessage> processPhoto(FileType fileType, Update update, Employee employee) {
        return CompletableFuture.supplyAsync(() -> {

                    final var chatId = update.getMessage().getChatId();
                    var photoSizeCount = update.getMessage().getPhoto().size();
                    var photoIndex = photoSizeCount > 1 ? update.getMessage().getPhoto().size() - 1 : 0;
                    var telegramPhoto = update.getMessage().getPhoto().get(photoIndex);
                    var fileId = telegramPhoto.getFileId();
                    var response = getFilePath(fileId);

                    if (response.getStatusCode() == HttpStatus.OK) {
                        var persistentBinaryContent = getPersistentBinaryContent(response);
                        var transientAppPhoto = buildTransientAppPhoto(fileType, telegramPhoto, persistentBinaryContent);
                        final var saved = appPhotoRepository.save(transientAppPhoto);
                        employee.setAppPhotos(List.of(saved));

                    } else
                        throw new UploadFileException("Bad response from telegram service: " + response);

                    final var messageCompletableFuture = buttonService.completeAddingEmployeeInfo(update, employee);
                    final var sendMessage = messageCompletableFuture.join();
                    final var replyMarkup = sendMessage.getReplyMarkup();

                    return SendMessage.builder()
                            .text("Attachment is saved!")
                            .replyMarkup(replyMarkup)
                            .chatId(chatId)
                            .build();
                }
        );
    }

    public void saveEmployee_Photo(Employee employee) {
        for (AppPhoto appPhoto : employee.getAppPhotos()) {
            if (appPhoto.getFileType().name().equals(FileType.EMPLOYEE_PHOTO.name())) {
                employeePhotoRepository.save(new EmployeePhoto(employee, appPhoto));
                return;
            }
        }
    }


    private record downloadDTO(URL link, byte[] bytes) {
    }

    private BinaryContent getPersistentBinaryContent(ResponseEntity<String> response) {
        var filePath = getFilePath(response);
        var fileByte = downloadFile(filePath);
        var transientBinaryContent = BinaryContent.builder()
                .fileAsArrayOfBytes(fileByte.bytes)
                .link(fileByte.link)
                .build();
        return binaryContentRepository.save(transientBinaryContent);
    }

    private String getFilePath(ResponseEntity<String> response) {
        var jsonObject = new JSONObject(response.getBody());
        return String.valueOf(jsonObject
                .getJSONObject("result")
                .getString("file_path"));
    }

    private AppDocument buildTransientAppDoc(FileType fileType, Document telegramDoc, BinaryContent persistentBinaryContent) {
        return AppDocument.builder()
                .telegramFileId(telegramDoc.getFileId())
                .docName(telegramDoc.getFileName())
                .binaryContent(persistentBinaryContent)
                .mimeType(telegramDoc.getMimeType())
                .fileType(fileType)
                .fileSize(telegramDoc.getFileSize())
                .linkForDownloading(persistentBinaryContent.getLink())
                .fileAsArrayOfBytes(persistentBinaryContent.getFileAsArrayOfBytes())
                .build();
    }

    private AppPhoto buildTransientAppPhoto(FileType fileType, PhotoSize telegramPhoto, BinaryContent persistentBinaryContent) {
        return AppPhoto.builder()
                .telegramFileId(telegramPhoto.getFileId())
                .fileSize(telegramPhoto.getFileSize())
                .linkForDownloading("")
                .fileType(fileType)
                .fileAsArrayOfBytes(persistentBinaryContent.getFileAsArrayOfBytes())
                .build();
    }

    private ResponseEntity<String> getFilePath(String fileId) {

        var restTemplate = new RestTemplate();
        var headers = new HttpHeaders();
        var request = new HttpEntity<>(headers);

        return restTemplate.exchange(
                fileInfoUri,
                HttpMethod.GET,
                request,
                String.class,
                token, fileId
        );
    }

    private downloadDTO downloadFile(String filePath) {
        var fullUri = fileStorageUri.replace("{token}", token)
                .replace("{filePath}", filePath);
        URL urlObj;
        try {
            urlObj = new URL(fullUri);
        } catch (MalformedURLException e) {
            throw new UploadFileException(e);
        }
        try (InputStream is = urlObj.openStream()) {
            return new downloadDTO(urlObj, is.readAllBytes());

        } catch (IOException e) {
            throw new UploadFileException(urlObj.toExternalForm(), e);
        }
    }

    public SendMediaGroup employeePhotos(Employee employee) {
        List<InputMedia> mediaPhotos = new ArrayList<>();
        final var appPhotos = employee.getAppPhotos();

        if (appPhotos != null) {
            for (AppPhoto appPhoto : appPhotos) {
                if (!appPhoto.getFileType().name().equals(FileType.EMPLOYEE_PHOTO.name())) {
                    InputMediaPhoto inputMediaPhoto = new InputMediaPhoto();
                    InputStream inputStream = new ByteArrayInputStream(appPhoto.getFileAsArrayOfBytes());
                    inputMediaPhoto.setMedia(inputStream, "photo.jpg");
                    mediaPhotos.add(inputMediaPhoto);
                }
            }
        }

        return SendMediaGroup.builder()
                .medias(mediaPhotos)
                .build();
    }

    public SendMediaGroup employeeDocuments(Employee employee) {

        List<InputMedia> mediaDocuments = new ArrayList<>();
        final var documents = employee.getDocuments();
        int i = 1;
        String fileName = "employee_doc_" + i + ".pdf";

        if (documents != null && !documents.isEmpty()) {
            for (AppDocument document : documents) {

                InputMediaDocument inputMediaDocument = new InputMediaDocument();
                InputStream inputStream = new ByteArrayInputStream(document.getFileAsArrayOfBytes());
                inputMediaDocument.setMedia(inputStream, fileName);
                mediaDocuments.add(inputMediaDocument);
                i++;
            }
            return SendMediaGroup.builder()
                    .medias(mediaDocuments)
                    .build();
        } else
            return null;
    }

    public String generateLink(Long docId, LinkType linkType) {
        var hash = cryptoTool.hashOf(docId);
        return "http://localhost:9090/" + linkType + "?id=" + hash;
    }
}