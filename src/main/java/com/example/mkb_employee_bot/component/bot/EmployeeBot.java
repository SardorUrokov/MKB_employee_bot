package com.example.mkb_employee_bot.component.bot;

import jakarta.ws.rs.NotFoundException;

import lombok.Data;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import com.example.mkb_employee_bot.entity.*;
import com.example.mkb_employee_bot.repository.*;
import com.example.mkb_employee_bot.entity.enums.*;
import com.example.mkb_employee_bot.service.BotService;
import com.example.mkb_employee_bot.service.FileService;
import com.example.mkb_employee_bot.service.ButtonService;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Data
@Component
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmployeeBot extends TelegramLongPollingBot {

    private static BotService botService;
    private static FileService fileService;
    private static ButtonService buttonService;

    private static UserRepository userRepository;
    private static PositionRepository positionRepository;
    private static EmployeeRepository employeeRepository;
    private static DepartmentRepository departmentRepository;
    private static ManagementRepository managementRepository;

    Long chatId;
    String userStage;
    String userStep;
    String userLanguage;
    FileType fileType;

    Education education = new Education();
    Education selectedEducation = new Education();
    Management prevManagement = new Management();

    Department prevDepartment = new Department();
    Department selectedDepartment = new Department();

    Position prevPosition = new Position();
    Position selectedPosition = new Position();

    Employee deletingEmployee = new Employee();
    Employee updatingEmployee = new Employee();
    Employee creatingEmployee = new Employee();

    String botUsername = "mkb_employees_bot";
    String botToken = "6608186289:AAER7qqqE-mNPMZCZrIj6zm8JS_q7o7eCmw";

    @SuppressWarnings("DataFlowIssue")
    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage()) {

            chatId = update.getMessage().getChatId();
            userLanguage = botService.getUserLanguage(chatId);
            userStage = userRepository.getUserStageByUserChatId(chatId);
            if (userStage == null)
                userStage = "";
            String userStepByUserChatId = userRepository.getUserStepByUserChatId(chatId);
            userStep = userStepByUserChatId == null ? "" : userStepByUserChatId;

            var userRole = botService.getUserRole(chatId);
            if (userRole == null)
                userRole = "USER";

            boolean isUser = userRole.equals("USER");
            final var isSuperAdmin = userRole.equals("SUPER_ADMIN");
            final var isAdmin = userRole.equals("ADMIN");

            Message message = update.getMessage();
            String messageText = message.getText() == null ? "" : message.getText();
            System.out.println("userStage: " + userStage);
            System.out.println("messageText: " + messageText);
            System.out.println("userStep: " + userStep + "\n");

            final var caseContainingList = employeeRepository.findByFullNameIgnoreCaseContainingAndIsDeletedFalse(messageText);
            final var isCaseContainingListEmpty = caseContainingList.isEmpty();
            final var messageSection = botService.getMessageSection(messageText);

            if (message.hasContact()) {
                CompletableFuture<Void> updateContactFuture = CompletableFuture.runAsync(
                        () -> botService.setPhoneNumber(update)
                );
                updateContactFuture.join();
                userRepository.updateUserStageByUserChatId(chatId, Stage.CONTACT_SHARED.name());
            }

            if ("/start".equals(messageText)) {

                String welcomeMessage = """
                        MKBank Xodimlari botiga xush kelibsiz!
                                    
                        Добро пожаловать в бот для сотрудников МКБанк!
                        """;

                sendTextMessage(chatId, welcomeMessage);
                CompletableFuture<SendMessage> messageCompletableFuture = buttonService.selectLanguageButtons(update);
                SendMessage sendMessage = messageCompletableFuture.join();
                try {
                    CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                    executeFuture.join();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            } else if (messageText.equals("\uD83C\uDDFA\uD83C\uDDFF") || messageText.equals("\uD83C\uDDF7\uD83C\uDDFA")) {

                CompletableFuture<SendMessage> setUserLanguageAndRequestContact = botService.setUserLanguage(update);
                SendMessage sendMessage = setUserLanguageAndRequestContact.join();
                try {
                    CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                    executeFuture.join();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            } else if ("Bosh Menu ↩️".equals(messageText) || "Главное Меню ↩️".equals(messageText)) {

                CompletableFuture<SendMessage> messageCompletableFuture;
                if (isAdmin || isSuperAdmin)
                    messageCompletableFuture = buttonService.superAdminButtons(update);
                else
                    messageCompletableFuture = buttonService.userButtons(update);

                SendMessage sendMessage = messageCompletableFuture.join();

                try {
                    CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                    executeFuture.join();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else if (("Изменить язык \uD83C\uDDF7\uD83C\uDDFA / \uD83C\uDDFA\uD83C\uDDFF".equals(messageText) || "Tilni o'zgartirish \uD83C\uDDFA\uD83C\uDDFF / \uD83C\uDDF7\uD83C\uDDFA".equals(messageText))) {

                CompletableFuture<SendMessage> sendMessageCompletableFuture = botService.changeLanguage(update);
                SendMessage sendMessage = sendMessageCompletableFuture.join();
                try {
                    CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                    executeFuture.join();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            } else if ((("Департаменты".equals(messageText) || "Departamentlar".equals(messageText)) && isUser) || ("Departamentlar ro'yhati".equals(messageText) || "Список Департаменты".equals(messageText)) && (isAdmin || isSuperAdmin)) {

                CompletableFuture<SendMessage> sendMessageCompletableFuture = buttonService.departmentSectionUserRoleButtons(update);
                SendMessage sendMessage = sendMessageCompletableFuture.join();
                try {
                    CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                    executeFuture.join();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else if ((("Отделы".equals(messageText) || "Boshqarmalar".equals(messageText)) && isUser) || ("Boshqarmalar ro'yhati".equals(messageText) || "Список Отделы".equals(messageText)) && (isAdmin || isSuperAdmin)) {

                CompletableFuture<SendMessage> sendMessageCompletableFuture = buttonService.managementSectionUserRoleButtons(update);
                SendMessage sendMessage = sendMessageCompletableFuture.join();
                try {
                    CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                    executeFuture.join();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else if ((("Должности".equals(messageText) || "Lavozimlar".equals(messageText)) && isUser) || ("Список Должностов".equals(messageText) || "Lavozimlar ro'yhati".equals(messageText)) && (isAdmin || isSuperAdmin)) {

                CompletableFuture<SendMessage> sendMessageCompletableFuture = buttonService.positionSectionUserRoleButtons(update);
                SendMessage sendMessage = sendMessageCompletableFuture.join();
                try {
                    CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                    executeFuture.join();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else if ((("Сотрудник".equals(messageText) || "Xodim".equals(messageText)) && isUser) || ("Найти Сотрудника".equals(messageText) || "Xodimni qidirish".equals(messageText)) && (isAdmin || isSuperAdmin)) {

                CompletableFuture<SendMessage> sendMessageCompletableFuture = buttonService.employeeSectionUserRoleButtons(update, "");
                SendMessage sendMessage = sendMessageCompletableFuture.join();
                try {
                    CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                    executeFuture.join();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else if (userStage.equals("SECTION_SELECTED") && isUser) {

                CompletableFuture<SendMessage> sendMessageCompletableFuture = new CompletableFuture<>();
                switch (messageSection) {
                    case "departmentSection" ->
                            sendMessageCompletableFuture = buttonService.departmentEmployees(update);
                    case "positionSection" -> sendMessageCompletableFuture = buttonService.positionEmployees(update);
                    case "managementSection" ->
                            sendMessageCompletableFuture = buttonService.managementEmployees(update);
                    default -> {
                        if (userLanguage.equals("UZ"))
                            sendTextMessage(chatId, "Iltimos, ro'yhatdagi bo'limlardan birini tanlang ❗️");
                        else sendTextMessage(chatId, "Пожалуйста, выберите один из разделов списка ❗️");
                    }
                }

                SendMessage sendMessage = sendMessageCompletableFuture.join();
                try {
                    CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                    executeFuture.join();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else if (!isCaseContainingListEmpty && (userStage.equals("ENTERED_EMPLOYEE_NAME_FOR_SEARCH_ROLE_USER"))) {

                CompletableFuture<SendMessage> sendMessageCompletableFuture = buttonService.findEmployeeSectionUserRoleButtons(update, "");
                SendMessage sendMessage = sendMessageCompletableFuture.join();
                try {
                    CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                    executeFuture.join();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else if (userStage.equals("SELECTED_EMPLOYEE_NAME_FOR_SEARCH_ROLE_USER")) {

                CompletableFuture<SendPhoto> sendMessageCompletableFuture = botService.getSelectedEmployeeInfo(update);
                SendPhoto sendMessage = sendMessageCompletableFuture.join();
                try {
                    CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                    executeFuture.join();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                chatId = update.getMessage().getChatId();
                final var employee = employeeRepository
                        .findByFullNameAndDeletedFalse(update.getMessage().getText())
                        .orElseThrow(NotFoundException::new);

                final var appPhotos = fileService.employeePhotos(employee);
                final var appDocuments = fileService.employeeDocuments(employee);

                if (!appPhotos.isEmpty()) {
                    for (AppPhoto appPhoto : appPhotos) {
                        if (!(appPhoto.getFileType().name().equals(FileType.EMPLOYEE_PHOTO.name()))) {
                            byte[] fileAsArrayOfBytes = appPhoto.getFileAsArrayOfBytes();
                            InputStream inputStream = new ByteArrayInputStream(fileAsArrayOfBytes);
                            InputFile photoInputFile = new InputFile(inputStream, "photo.jpg");

                            sendPhoto(chatId, appPhoto.getFileType().name(), photoInputFile);
                        }
                    }
                }

                if (!appDocuments.isEmpty()) {
                    for (AppDocument appDocument : appDocuments) {
                        byte[] fileAsArrayOfBytes = appDocument.getFileAsArrayOfBytes();
                        InputStream inputStream = new ByteArrayInputStream(fileAsArrayOfBytes);
                        InputFile docInputFile = new InputFile(inputStream, appDocument.getDocName());

                        sendDocument(chatId, appDocument.getFileType().name(), docInputFile);
                    }
                }

            } else if (("Xodimlar".equals(messageText) || "Сотрудники".equals(messageText)) && (isAdmin || isSuperAdmin)) {

                CompletableFuture<SendMessage> sendMessageCompletableFuture = buttonService.employeeSectionButtons(update);
                SendMessage sendMessage = sendMessageCompletableFuture.join();
                try {
                    CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                    executeFuture.join();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            } else if (("Должности".equals(messageText) || "Lavozimlar".equals(messageText)) && (isAdmin || isSuperAdmin)) {

                CompletableFuture<SendMessage> sendMessageCompletableFuture = buttonService.positionSectionButtons(update);
                SendMessage sendMessage = sendMessageCompletableFuture.join();
                try {
                    CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                    executeFuture.join();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            } else if (("Departamentlar".equals(messageText) || "Департаменты".equals(messageText)) && (isAdmin || isSuperAdmin)) {

                CompletableFuture<SendMessage> sendMessageCompletableFuture = buttonService.departmentSectionButtons(update);
                SendMessage sendMessage = sendMessageCompletableFuture.join();
                try {
                    CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                    executeFuture.join();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            } else if (("Boshqarmalar".equals(messageText) || "Отделы".equals(messageText)) && (isAdmin || isSuperAdmin)) {

                CompletableFuture<SendMessage> sendMessageCompletableFuture = buttonService.managementSectionButtons(update);
                SendMessage sendMessage = sendMessageCompletableFuture.join();
                try {
                    CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                    executeFuture.join();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else if (("Adminlar".equals(messageText) || "Админы".equals(messageText)) && (isAdmin || isSuperAdmin)) {

                CompletableFuture<SendMessage> setUserLanguageAndRequestContact;

                if (isAdmin) setUserLanguageAndRequestContact = buttonService.adminSectionAdminRoleButtons(update);
                else setUserLanguageAndRequestContact = buttonService.adminSectionSuperAdminRoleButtons(update);

                SendMessage sendMessage = setUserLanguageAndRequestContact.join();
                try {
                    CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                    executeFuture.join();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            } else if ("Xodim qo'shish".equals(messageText) || "Добавить Сотрудники".equals(messageText) && (isAdmin || isSuperAdmin)) {

                CompletableFuture<SendMessage> sendMessageCompletableFuture = buttonService.askSelectManagementForCreatingPosition(update, "forCreatingEmployee");
                SendMessage sendMessage = sendMessageCompletableFuture.join();
                try {
                    CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                    executeFuture.join();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            } else if (userStage.equals("MANAGEMENT_SELECTED_FOR_CREATING_EMPLOYEE") && (isAdmin || isSuperAdmin)) {

                prevManagement = managementRepository.findByName(messageText).orElseThrow();
                CompletableFuture<SendMessage> setUserLanguageAndRequestContact = buttonService.askSelectPositionForUpdating(prevManagement, update, "forCreatingEmployee");
                SendMessage sendMessage = setUserLanguageAndRequestContact.join();

                try {
                    CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                    executeFuture.join();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            } else if ("Lavozim yaratish".equals(messageText) || "Создать должность".equals(messageText) && (isAdmin || isSuperAdmin)) {

                CompletableFuture<SendMessage> setUserLanguageAndRequestContact = buttonService.askPositionNameForCreatingEmployee(update);
                SendMessage sendMessage = setUserLanguageAndRequestContact.join();

                try {
                    CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                    executeFuture.join();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else if (userStage.equals("ENTERED_POSITION_NAME_FOR_CREATING_EMPLOYEE") && (isAdmin || isSuperAdmin)) {

                selectedPosition = positionRepository.save(
                        new Position(messageText, prevManagement)
                );
                CompletableFuture<SendMessage> setUserLanguageAndRequestContact = buttonService.askSelectPositionForUpdating(prevManagement, update, "forCreatingEmployee");
                SendMessage sendMessage = setUserLanguageAndRequestContact.join();

                try {
                    CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                    executeFuture.join();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            } else if (("Начать редактирование ✅".equals(messageText) || "Tahrirlashni boshlash ✅".equals(messageText) && (isAdmin || isSuperAdmin))) {

                CompletableFuture<SendMessage> setUserLanguageAndRequestContact = buttonService.askSectionForUpdatingEmployee(update);
                SendMessage sendMessage = setUserLanguageAndRequestContact.join();

                try {
                    CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                    executeFuture.join();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            } else if (("Lavozimi".equals(messageText) || "Должность".equals(messageText)) && userStage.equals("SELECTED_EMPLOYEE_UPDATING_INFO_ROLE_ADMIN") && (isAdmin || isSuperAdmin)) {

                final var messageCompletableFuture = buttonService.askSelectManagementForCreatingPosition(update, "forUpdatingEmployeePosition");
                SendMessage sendMessage = messageCompletableFuture.join();
                try {
                    CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                    executeFuture.join();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else if (userStage.equals("SELECTED_EMPLOYEE_UPDATING_INFO_ROLE_ADMIN") && (!userStep.equals("")) && (isAdmin || isSuperAdmin)) {

                CompletableFuture<SendMessage> setUserLanguageAndRequestContact = botService.updateEmployee(update, updatingEmployee);
                SendMessage sendMessage = setUserLanguageAndRequestContact.join();
                try {
                    CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                    executeFuture.join();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                if (!("Bekor qilish ❌".equals(messageText) || "Отменить ❌".equals(messageText))) {
                    if (userLanguage.equals("RU"))
                        sendTextMessage(chatId, "Информация о сотруднике изменена ❗️");
                    else
                        sendTextMessage(chatId, "Xodim ma'lumotlari o'zgartirildi ❗️");
                }

            } else if (userStage.equals("SELECTED_EMPLOYEE_UPDATING_INFO_ROLE_ADMIN") && (isAdmin || isSuperAdmin)) {

                CompletableFuture<SendMessage> setUserLanguageAndRequestContact = buttonService.askInfoForSelectedSection(update, updatingEmployee);
                SendMessage sendMessage = setUserLanguageAndRequestContact.join();
                try {
                    CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                    executeFuture.join();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            } else if (userStage.equals("EMPLOYEE_UPDATING_POSITION_SELECTED") && (isAdmin || isSuperAdmin)) {

                selectedPosition = positionRepository.findByNameAndManagement(messageText, prevManagement.getId()).orElseThrow();
                updatingEmployee.setPosition(selectedPosition); //update employee's new position

                CompletableFuture<SendMessage> setUserLanguageAndRequestContact = botService.updateEmployee(update, updatingEmployee);
                SendMessage sendMessage = setUserLanguageAndRequestContact.join();
                try {
                    CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                    executeFuture.join();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            } else if (userStage.equals("SELECTED_EMPLOYEE_NAME_FOR_UPDATING_ROLE_ADMIN") && (isAdmin || isSuperAdmin)) {

                updatingEmployee = employeeRepository.findByFullNameAndDeletedFalse(messageText).orElseThrow();
                CompletableFuture<SendMessage> setUserLanguageAndRequestContact = buttonService.askConfirmationForDeletingEmployee(update, "forUpdating");
                SendMessage sendMessage = setUserLanguageAndRequestContact.join();

                try {
                    CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                    executeFuture.join();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                if (userLanguage.equals("RU"))
                    sendTextMessage(chatId, "Начать редактирование ⁉️");
                else
                    sendTextMessage(chatId, "Tahrirlashni boshlaysizmi ⁉️");

            } else if (userStep.equals("SELECTED_UPDATING_EDUCATION_TYPE") && (isAdmin || isSuperAdmin)) {

                selectedEducation.setType(EduType.valueOf(messageText));

                if (userLanguage.equals("RU"))
                    sendTextMessage(chatId, "Введите заново название учебного заведения ");
                else
                    sendTextMessage(chatId, "Ta'lim muassasa nomini qaytadan kiriting ");

                userRepository.updateUserStepByUserChatId(chatId, Stage.ENTERED_UPDATING_EDUCATION_NAME.name());

            } else if (userStep.equals("ENTERED_UPDATING_EDUCATION_NAME") && (isAdmin || isSuperAdmin)) {

                selectedEducation.setName(messageText);
                if (userLanguage.equals("RU"))
                    sendTextMessage(chatId, "Заново введите направление обучения ");
                else
                    sendTextMessage(chatId, "Ta'lim yo'nalishini qaytadan kiriting ");

                userRepository.updateUserStepByUserChatId(chatId, Stage.ENTERED_UPDATING_EDUCATION_FIELD.name());

            } else if (userStep.equals("ENTERED_UPDATING_EDUCATION_FIELD") && (isAdmin || isSuperAdmin)) {

                selectedEducation.setEducationField(messageText);

                if (userLanguage.equals("UZ"))
                    sendTextMessage(chatId, """
                            O'quv yili muddatlarini qaytadan kiriting:
                                    
                            ❗️Namuna: 2018-2022;
                            ❗️Agar hozirda davom etayotgan bo'lsa: 2020-Present""");
                else
                    sendTextMessage(chatId, """
                            Заново введите сроки учебного года:

                            ❗️Образец: 2018-2022;
                            ❗️Если в данный момент продолжается: 2020-Present""");

                userRepository.updateUserStepByUserChatId(chatId, Stage.ENTERED_UPDATING_EDUCATION_PERIOD.name());

            } else if (userStep.equals("ENTERED_UPDATING_EDUCATION_PERIOD") && (isAdmin || isSuperAdmin)) {

                final var checkedEduPeriod = buttonService.checkEduPeriod(messageText);
                if (checkedEduPeriod) {
                    final var period = buttonService.getDatesFromPeriod(messageText);
                    final var startDate = period[0];
                    final var endDate = period[1];

                    selectedEducation.setStartedDate(startDate);
                    selectedEducation.setEndDate(endDate);

                    final var messageCompletableFuture = buttonService.saveUpdatingEducationInfo(update, updatingEmployee, selectedEducation);
                    final var sendMessage = messageCompletableFuture.join();

                    try {
                        CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                    try {
                                        execute(sendMessage);
                                    } catch (TelegramApiException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                        );
                        executeFuture.join();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    final String info;
                    if (userLanguage.equals("UZ")) {
                        info = buttonService.getEmployeeInfoForUserLanguage_UZ(updatingEmployee);
                    } else {
                        info = buttonService.getEmployeeInfoForUserLanguage_RU(updatingEmployee);
                    }
                    sendTextMessage(chatId, info);

                } else {
                    if (userLanguage.equals("UZ"))
                        messageText = """
                                ❌Muddat oralig'i noto'g'ri kiritildi. Sana formatini to'g'ri kiriting:

                                ❗️Namuna: 2018-2022;
                                ❗️Agar hozirda davom etayotgan bo'lsa: 2020-Present""";
                    else
                        messageText = """
                                ❌Неверно введен сроки выполнения. Введите правильный формат даты:
                                                                             
                                ❗️Образец: 2018-2022;
                                ❗️Если в настоящее время продолжается: (2020-Present)""";

                    sendTextMessage(chatId, messageText);
                }

            } else if (userStage.equals("SELECTED_EMPLOYEE_UPDATING_EDUCATION") && (isAdmin || isSuperAdmin)) {

                if ("To'xtatish 🛑".equals(messageText) || "Остановить 🛑".equals(messageText)) {
                    if (userLanguage.equals("RU"))
                        sendTextMessage(chatId, "Процесс остановлен ❗️");
                    else
                        sendTextMessage(chatId, "Jarayon to'xtatildi ❗️");

                    final var chatId = update.getMessage().getChatId();
                    userRepository.updateUserStageByUserChatId(chatId, Stage.STARTED.name());
                    userRepository.updateUserStepByUserChatId(chatId, "");

                    final var messageCompletableFuture = buttonService.employeeSectionButtons(update);
                    SendMessage sendMessage = messageCompletableFuture.join();

                    try {
                        CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                    try {
                                        execute(sendMessage);
                                    } catch (TelegramApiException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                        );
                        executeFuture.join();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                } else {

                    selectedEducation = buttonService.getSelectedEducation(messageText, updatingEmployee);
                    CompletableFuture<SendMessage> messageCompletableFuture = buttonService.askEnterUpdatingEducationInfos(update, selectedEducation);
                    SendMessage sendMessage = messageCompletableFuture.join();

                    try {
                        CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                    try {
                                        execute(sendMessage);
                                    } catch (TelegramApiException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                        );
                        executeFuture.join();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                    if (userLanguage.equals("RU"))
                        sendTextMessage(chatId, "Выберите заново уровень образования: ⬇️");
                    else
                        sendTextMessage(chatId, "Ta'lim bosqichini qaytadan tanlang ⬇️");
                }

            } else if (userStage.equals("ENTERED_EMPLOYEE_NAME_FOR_UPDATING_ROLE_ADMIN") && (isAdmin || isSuperAdmin)) {

                CompletableFuture<SendMessage> setUserLanguageAndRequestContact = buttonService.findEmployeeSectionUserRoleButtons(update, "forUpdating");
                SendMessage sendMessage = setUserLanguageAndRequestContact.join();
                try {
                    CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }

                            }
                    );
                    executeFuture.join();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            } else if (("Xodimni tahrirlash".equals(messageText) || "Редактировать Сотрудник".equals(messageText)) && (isAdmin || isSuperAdmin)) {

                CompletableFuture<SendMessage> setUserLanguageAndRequestContact = buttonService.employeeSectionUserRoleButtons(update, "forUpdating");
                SendMessage sendMessage = setUserLanguageAndRequestContact.join();
                try {
                    CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                    executeFuture.join();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            } else if ("To'xtatish 🛑".equals(messageText) || "Остановить 🛑".equals(messageText) && (isAdmin || isSuperAdmin)) {

                if (userLanguage.equals("UZ"))
                    sendTextMessage(chatId, "Jarayon to'xtatildi❗️");
                else
                    sendTextMessage(chatId, "Процесс остановлен❗️");

                buttonService.retryUserSteps();
                education = new Education();
                creatingEmployee = new Employee();

                CompletableFuture<SendMessage> setUserLanguageAndRequestContact = buttonService.employeeSectionButtons(update);
                SendMessage sendMessage = setUserLanguageAndRequestContact.join();

                try {
                    CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                    executeFuture.join();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            } else if (("O'tkazib yuborish ⏩".equals(messageText) || "Пропустить ⏩".equals(messageText)) && (isAdmin || isSuperAdmin)) {

                CompletableFuture<SendMessage> completableFuture;

                if (creatingEmployee != null) {
                    if (userLanguage.equals("UZ"))
                        sendTextMessage(chatId, "Saqlash uchun xodimning ma'lumotlarini tasdiqlaysizmi? ⬇️");
                    else
                        sendTextMessage(chatId, "Подтвердите информацию о сотруднике для сохранения? ⬇️");

                    completableFuture = buttonService.completeAddingEmployeeInfo(update, creatingEmployee);
                } else
                    completableFuture = buttonService.superAdminButtons(update);

                SendMessage sendMessage = completableFuture.join();
                try {
                    CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                    executeFuture.join();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            } else if ((userStage.equals(Stage.SELECTED_EMPLOYEE_2ND_EDUCATION_TYPE.name()) || (userStep.equals(Stage.ENTERED_EMPLOYEE_2ND_EDUCATION_PERIOD.name()))) && (isAdmin || isSuperAdmin)) {

                CompletableFuture<SendMessage> messageCompletableFuture = buttonService.enterSecondEducationInfo(update, education, creatingEmployee);
                SendMessage sendMessage = messageCompletableFuture.join();
                try {
                    CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                    executeFuture.join();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            } else if (("Yana ta'lim ma'lumoti qo'shish ➕".equals(messageText) || "Еще одну образовательную информацию ➕".equals(messageText)) && (isAdmin || isSuperAdmin)) {

                CompletableFuture<SendMessage> messageCompletableFuture = buttonService.addEducationAgain(update);
                SendMessage sendMessage = messageCompletableFuture.join();
                try {
                    CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                    executeFuture.join();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            } else if (("Yana fayl qo'shish ➕".equals(messageText) || "Добавить вложение еще раз ➕".equals(messageText)) && (isAdmin || isSuperAdmin)) {

                final var messageCompletableFuture = buttonService.sendAttachmentAgain(update);
                SendMessage sendMessage = messageCompletableFuture.join();
                try {
                    CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                    executeFuture.join();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            } else if ((Stage.SELECTED_EMPLOYEE_FILE_TYPE.name().equals(userStep)) || ("Tasdiqlash ✅".equals(messageText) || "Потвердить ✅".equals(messageText))) {

                CompletableFuture<SendMessage> sendMessageCompletableFuture;

                if (("Tasdiqlash ✅".equals(messageText) || "Потвердить ✅".equals(messageText))) {
                    sendMessageCompletableFuture = botService.createEmployee(creatingEmployee, update);
                    creatingEmployee = new Employee();
                    education = new Education();

                } else if (("Bekor qilish ❌".equals(messageText) || "Отменить ❌".equals(messageText))) {
                    sendMessageCompletableFuture = buttonService.cancelledConfirmation(update, "forCreatingEmployee");
                    creatingEmployee = new Employee();
                    education = new Education();

                    buttonService.retryUserSteps();
                } else {
                    fileType = FileType.valueOf(messageText);
                    sendMessageCompletableFuture = buttonService.askSendAttachment(update);
                }

                SendMessage sendMessage = sendMessageCompletableFuture.join();
                try {
                    CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                    executeFuture.join();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else if (Stage.ATTACHMENT_SHARED.name().equals(userStep)) {

                CompletableFuture<SendMessage> sendMessageCompletableFuture = new CompletableFuture<>();
                if (message.hasDocument())
                    sendMessageCompletableFuture = fileService.processDoc(fileType, update, creatingEmployee);
                else if (message.hasPhoto())
                    sendMessageCompletableFuture = fileService.processPhoto(fileType, update, creatingEmployee);

                userRepository.updateUserStepByUserChatId(chatId, "");

                SendMessage sendMessage = sendMessageCompletableFuture.join();
                try {
                    CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                    executeFuture.join();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                if (userLanguage.equals("RU"))
                    sendTextMessage(chatId, "Подтвердить сохранение ️⁉️");
                else
                    sendTextMessage(chatId, "Saqlashni tasdiqlaysizmi ⁉️");

            } else if ((userStage.equals("POSITION_FOR_CREATING_EMPLOYEE") || !userStep.equals("")) && (isAdmin || isSuperAdmin)) {

                if ("Отменить ❌".equals(messageText) || "Bekor qilish ❌".equals(messageText)) {
                    if (userLanguage.equals("RU"))
                        sendTextMessage(chatId, "Процесс отменено ❗️");
                    else
                        sendTextMessage(chatId, "Jarayon bekor qilindi ❗️");

                    final var messageCompletableFuture = buttonService.employeeSectionButtons(update);
                    final var sendMessage = messageCompletableFuture.join();
                    try {
                        CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                    try {
                                        execute(sendMessage);
                                    } catch (TelegramApiException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                        );
                        executeFuture.join();

                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                } else {

                    if ("personalInfo".equals(userStep)) {
                        if (creatingEmployee.getPosition() == null) {
                            selectedPosition = positionRepository.findByNameAndManagement(messageText, prevManagement.getId()).orElseThrow();
                            creatingEmployee.setPosition(selectedPosition);
                        }
                    } else if (Stage.ENTERED_EMPLOYEE_NAME_ROLE_ADMIN.name().equals(userStep))
                        creatingEmployee.setFullName(messageText);

                    else if (Stage.ENTERED_EMPLOYEE_PHONE_NUMBER_ROLE_ADMIN.name().equals(userStep))
                        creatingEmployee.setPhoneNumber(messageText);

                    else if (Stage.ENTERED_EMPLOYEE_BIRTHDATE_ROLE_ADMIN.name().equals(userStep)) {
                        creatingEmployee.setDateOfBirth(messageText);
                        creatingEmployee.setAge(buttonService.getAgeFromBirthDate(messageText));
                    } else if (Stage.ENTERED_EMPLOYEE_NATIONALITY.name().equals(userStep))
                        creatingEmployee.setNationality(messageText);

                    else if (Stage.SELECTED_EMPLOYEE_EDUCATION_TYPE.name().equals(userStep))
                        education.setType(EduType.valueOf(messageText));

                    else if (Stage.ENTERED_EMPLOYEE_EDUCATION_NAME.name().equals(userStep))
                        education.setName(messageText);

                    else if (Stage.ENTERED_EMPLOYEE_EDUCATION_FIELD.name().equals(userStep))
                        education.setEducationField(messageText);

                    else if (Stage.ENTERED_EMPLOYEE_EDUCATION_PERIOD.name().equals(userStep)) {

                        String[] dateFromPeriod = buttonService.getDatesFromPeriod(messageText);
                        education.setStartedDate(dateFromPeriod[0]);
                        education.setEndDate(dateFromPeriod[1]);

                        final var employeeEducations = creatingEmployee.getEducations();
                        employeeEducations.add(education);
                        creatingEmployee.setEducations(employeeEducations);
                        education = new Education();

                    } else if (Stage.ENTERED_EMPLOYEE_SKILLS.name().equals(userStep)) {

                        final var employeeSkills = creatingEmployee.getSkills();
                        for (String s : buttonService.splitSkills(messageText)) {
                            employeeSkills.add(new Skill(s));
                        }
                        creatingEmployee.setSkills(employeeSkills);
                    }

                    CompletableFuture<SendMessage> setUserLanguageAndRequestContact = buttonService.askInformationOfEmployeeForCreating(update, userStep);
                    SendMessage sendMessage = setUserLanguageAndRequestContact.join();
                    try {
                        CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                    try {
                                        execute(sendMessage);
                                    } catch (TelegramApiException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                        );
                        executeFuture.join();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }

            } else if (userStage.equals("CONFIRMATION_FOR_DELETING_EMPLOYEE") && (isAdmin || isSuperAdmin)) {
                CompletableFuture<SendMessage> sendMessageCompletableFuture = new CompletableFuture<>();

                if ("O'chirishni Tasdiqlash ✅".equals(messageText) || "Потвердить Удаление✅".equals(messageText))
                    sendMessageCompletableFuture = botService.deleteEmployee(deletingEmployee, update);

                else if ("O'chirishni Bekor qilish ❌".equals(messageText) || "Отменить Удаление❌".equals(messageText))
                    sendMessageCompletableFuture = buttonService.cancelledConfirmation(update, "forDeletingEmployee");

                SendMessage sendMessage = sendMessageCompletableFuture.join();
                try {
                    CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                    executeFuture.join();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            } else if (userStage.equals("SELECTED_EMPLOYEE_NAME_FOR_DELETING_ROLE_ADMIN") && (isAdmin || isSuperAdmin)) {

                deletingEmployee = employeeRepository.findByFullNameAndDeletedFalse(messageText).orElseThrow();
                CompletableFuture<SendMessage> setUserLanguageAndRequestContact = buttonService.askConfirmationForDeletingEmployee(update, "forDeleting");
                SendMessage sendMessage = setUserLanguageAndRequestContact.join();

                try {
                    CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                    executeFuture.join();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                if (userLanguage.equals("RU"))
                    sendTextMessage(chatId, "Подтвердить удаление ️");
                else
                    sendTextMessage(chatId, "O'chirishni tasdiqlaysizmi ⁉️");

            } else if (userStage.equals("ENTERED_EMPLOYEE_NAME_FOR_DELETE_ROLE_USER") && (isAdmin || isSuperAdmin)) {

                CompletableFuture<SendMessage> setUserLanguageAndRequestContact = buttonService.findEmployeeSectionUserRoleButtons(update, "forDeleting");
                SendMessage sendMessage = setUserLanguageAndRequestContact.join();
                try {
                    CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }

                            }
                    );
                    executeFuture.join();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            } else if ("Удалить Сотрудрик".equals(messageText) || "Xodimni o'chirish".equals(messageText) && (isAdmin || isSuperAdmin)) {

                CompletableFuture<SendMessage> setUserLanguageAndRequestContact = buttonService.employeeSectionUserRoleButtons(update, "forDeleting");
                SendMessage sendMessage = setUserLanguageAndRequestContact.join();
                try {
                    CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                    executeFuture.join();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            } else if ("Добавить Департамент".equals(messageText) || "Departament qo'shish".equals(messageText) && (isAdmin || isSuperAdmin)) {

                CompletableFuture<SendMessage> setUserLanguageAndRequestContact = buttonService.askNameForCreatingDepartment(update);
                SendMessage sendMessage = setUserLanguageAndRequestContact.join();
                try {
                    CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                    executeFuture.join();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            } else if (("Добавить Отдел".equals(messageText) || "Boshqarma qo'shish".equals(messageText) && (isAdmin || isSuperAdmin))) {

                CompletableFuture<SendMessage> messageCompletableFuture = buttonService.askSelectDepartmentForCreateManagement(update);
                SendMessage sendMessage = messageCompletableFuture.join();
                try {
                    CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                    executeFuture.join();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            } else if ("Boshqarmani o'chirish".equals(messageText) || "Удалить Отдел".equals(messageText)) {

                CompletableFuture<SendMessage> messageCompletableFuture = buttonService.askSelectManagementForDeleting(update);
                SendMessage sendMessage = messageCompletableFuture.join();
                try {
                    CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                    executeFuture.join();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            } else if ("Редактировать Отдел".equals(messageText) || "Boshqarmalarni tahrirlash".equals(messageText) && (isAdmin || isSuperAdmin)) {

                CompletableFuture<SendMessage> messageCompletableFuture = buttonService.askSelectDepartmentForUpdatingManagement(update, "forSelecting");
                SendMessage sendMessage = messageCompletableFuture.join();
                try {
                    CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                    executeFuture.join();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            } else if ("Добавить Должность".equals(messageText) || "Lavozim qo'shish".equals(messageText) && (isAdmin || isSuperAdmin)) {

                CompletableFuture<SendMessage> messageCompletableFuture = buttonService.askSelectManagementForCreatingPosition(update, "forCreating");
                SendMessage sendMessage = messageCompletableFuture.join();
                try {
                    CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                    executeFuture.join();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            } else if (userStage.equals("ENTER_NAME_FOR_CREATING_POSITION_NAME") && (isAdmin || isSuperAdmin)) {

                CompletableFuture<SendMessage> messageCompletableFuture = botService.createPosition(prevManagement, update);
                SendMessage sendMessage = messageCompletableFuture.join();
                try {
                    CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                    executeFuture.join();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            } else if (userStage.equals("MANAGEMENT_SELECTED_FOR_CREATING_POSITION") && (isAdmin || isSuperAdmin)) {

                prevManagement = managementRepository.findByName(messageText).orElseThrow();
                CompletableFuture<SendMessage> messageCompletableFuture = buttonService.askNameForCreatingPosition(update, "forCreating");
                SendMessage sendMessage = messageCompletableFuture.join();
                try {
                    CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                    executeFuture.join();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else if ("Lavozimni o'chirish".equals(messageText) || "Удалить Должность".equals(messageText) && (isAdmin || isSuperAdmin)) {

                CompletableFuture<SendMessage> messageCompletableFuture = buttonService.askSelectPositionForDeleting(update);
                SendMessage sendMessage = messageCompletableFuture.join();
                try {
                    CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                    executeFuture.join();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            } else if (userStage.equals("POSITION_SELECTED_FOR_DELETING") && (isAdmin || isSuperAdmin)) {

                CompletableFuture<SendMessage> messageCompletableFuture = botService.deletePosition(update);
                SendMessage sendMessage = messageCompletableFuture.join();
                try {
                    CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                    executeFuture.join();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            } else if ("Редактировать Должность".equals(messageText) || "Lavozimni tahrirlash".equals(messageText) && (isAdmin || isSuperAdmin)) {

                CompletableFuture<SendMessage> messageCompletableFuture = buttonService.askSelectManagementForCreatingPosition(update, "forUpdating");
                SendMessage sendMessage = messageCompletableFuture.join();
                try {
                    CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                    executeFuture.join();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            } else if (userStage.equals("MANAGEMENT_SELECTED_FOR_UPDATING_EMPLOYEE_POSITION") && (isAdmin || isSuperAdmin)) {

                prevManagement = managementRepository.findByName(messageText).orElseThrow();
                CompletableFuture<SendMessage> messageCompletableFuture = buttonService.askSelectPositionForUpdating(prevManagement, update, "forUpdatingEmployeePosition");
                SendMessage sendMessage = messageCompletableFuture.join();
                try {
                    CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                    executeFuture.join();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            } else if (userStage.equals("MANAGEMENT_SELECTED_FOR_UPDATING_POSITION") && (isAdmin || isSuperAdmin)) {

                prevManagement = managementRepository.findByName(messageText).orElseThrow();
                CompletableFuture<SendMessage> messageCompletableFuture = buttonService.askSelectPositionForUpdating(prevManagement, update, "");
                SendMessage sendMessage = messageCompletableFuture.join();
                try {
                    CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                    executeFuture.join();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            } else if (userStage.equals("POSITION_SELECTED_FOR_UPDATING") && (isAdmin || isSuperAdmin)) {

                String text;
                if (userLanguage.equals("UZ")) text = "Juda soz! Endi tahrirlashni boshlaymiz.";
                else text = "Отлично! Теперь приступим к редактированию.";
                sendTextMessage(chatId, text);

                prevPosition = positionRepository.findByName(messageText).orElseThrow();
                CompletableFuture<SendMessage> messageCompletableFuture = buttonService.askNameForCreatingPosition(update, "forUpdating");
                final var sendMessage = messageCompletableFuture.join();

                try {
                    CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );

                    executeFuture.join();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            } else if (userStage.equals("ENTER_NAME_FOR_UPDATE_POSITION") && (isAdmin || isSuperAdmin)) {

                CompletableFuture<SendMessage> messageCompletableFuture = botService.updatePosition(update, prevPosition);
                SendMessage sendMessage = messageCompletableFuture.join();
                try {
                    CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                    executeFuture.join();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            } else if (userStage.equals("MANAGEMENT_SELECTED_FOR_DELETING") && (isAdmin || isSuperAdmin)) {

                CompletableFuture<SendMessage> messageCompletableFuture = botService.deleteManagement(update);
                SendMessage sendMessage = messageCompletableFuture.join();
                try {
                    CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                    executeFuture.join();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            } else if (userStage.equals("DEPARTMENT_SELECTED_FOR_UPDATING_MANAGEMENT") && (isAdmin || isSuperAdmin)) {

                prevDepartment = departmentRepository.findByName(messageText).orElseThrow();
                CompletableFuture<SendMessage> messageCompletableFuture = buttonService.askSelectManagementForUpdating(prevDepartment, update);
                SendMessage sendMessage = messageCompletableFuture.join();
                try {
                    CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                    executeFuture.join();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            } else if (userStage.equals("ENTER_NAME_FOR_SAVING_UPDATED_MANAGEMENT") && (isAdmin || isSuperAdmin)) {

                CompletableFuture<SendMessage> messageCompletableFuture = botService.updateManagement(update, prevManagement);
                SendMessage sendMessage = messageCompletableFuture.join();
                try {
                    CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                    executeFuture.join();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            } else if (userStage.equals("DEPARTMENT_SELECTED_FOR_SAVING_UPDATED_MANAGEMENT") && (isAdmin || isSuperAdmin)) {

                selectedDepartment = departmentRepository.findByName(messageText).orElseThrow();
                CompletableFuture<SendMessage> messageCompletableFuture = buttonService.askingNameForCreatingManagement(update, "forSaving");
                SendMessage sendMessage = messageCompletableFuture.join();
                try {
                    CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                    executeFuture.join();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            } else if (userStage.equals("MANAGEMENT_SELECTED_FOR_UPDATING") && (isAdmin || isSuperAdmin)) {

                String text;
                if (userLanguage.equals("UZ")) text = "Juda soz! Endi tahrirlashni boshlaymiz.";
                else text = "Отлично! Теперь приступим к редактированию.";

                sendTextMessage(chatId, text);

                prevManagement = managementRepository.findByName(messageText).orElseThrow();
                CompletableFuture<SendMessage> messageCompletableFuture = buttonService.askSelectDepartmentForUpdatingManagement(update, "forSaving");
                final var sendMessage = messageCompletableFuture.join();
                try {
                    CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                    executeFuture.join();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            } else if (userStage.equals("DEPARTMENT_SELECTED_FOR_CREATING_MANAGEMENT") && (isAdmin || isSuperAdmin)) {

                selectedDepartment = departmentRepository.findByName(messageText).orElseThrow();
                CompletableFuture<SendMessage> messageCompletableFuture = buttonService.askingNameForCreatingManagement(update, "");
                SendMessage sendMessage = messageCompletableFuture.join();
                try {
                    CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                    executeFuture.join();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            } else if (userStage.equals("ENTER_NAME_FOR_CREATE_MANAGEMENT") && (isAdmin || isSuperAdmin)) {

                CompletableFuture<SendMessage> messageCompletableFuture = botService.createManagement(selectedDepartment, update);
                SendMessage sendMessage = messageCompletableFuture.join();

                try {
                    CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                    executeFuture.join();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            } else if (userStage.equals("ENTER_NAME_FOR_CREATE_DEPARTMENT") && (isAdmin || isSuperAdmin)) {

                CompletableFuture<SendMessage> messageCompletableFuture = botService.createDepartment(update);
                SendMessage sendMessage = messageCompletableFuture.join();
                try {
                    CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                    executeFuture.join();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            } else if ("Departamentni o'chirish".equals(messageText) || "Удалить Департамент".equals(messageText) && (isAdmin || isSuperAdmin)) {

                CompletableFuture<SendMessage> messageCompletableFuture = buttonService.getDepartmentListForDeleting(update);
                SendMessage sendMessage = messageCompletableFuture.join();
                try {
                    CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                    executeFuture.join();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else if (userStage.equals("DEPARTMENT_SELECTED_FOR_DELETING") && (isAdmin || isSuperAdmin)) {

                CompletableFuture<SendMessage> messageCompletableFuture = botService.deleteDepartment(update);
                SendMessage sendMessage = messageCompletableFuture.join();
                try {
                    CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                    executeFuture.join();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else if (("Departamentni tahrirlash".equals(messageText) || "Редактировать Департамент".equals(messageText)) && (isAdmin || isSuperAdmin)) {

                CompletableFuture<SendMessage> messageCompletableFuture = buttonService.getDepartmentListForUpdating(update);
                SendMessage sendMessage = messageCompletableFuture.join();
                try {
                    CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                    executeFuture.join();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else if (userStage.equals("DEPARTMENT_SELECTED_FOR_UPDATING") && (isAdmin || isSuperAdmin)) {

                prevDepartment = departmentRepository.findByName(messageText).orElseThrow();
                CompletableFuture<SendMessage> messageCompletableFuture = buttonService.askNameForUpdatingDepartment(update);
                SendMessage sendMessage = messageCompletableFuture.join();

                try {
                    CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                    executeFuture.join();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else if (userStage.equals("ENTER_NAME_FOR_UPDATE_DEPARTMENT") && (isAdmin || isSuperAdmin)) {

                CompletableFuture<SendMessage> messageCompletableFuture = botService.updateDepartment(update, prevDepartment);
                SendMessage sendMessage = messageCompletableFuture.join();
                try {
                    CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                        try {
                            execute(sendMessage);
                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        }
                    });
                    executeFuture.join();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else if (("Инфо Администратора".equals(messageText) || "Admin ma'lumotlari".equals(messageText)) && (isAdmin || isSuperAdmin)) {

                CompletableFuture<SendMessage> setUserLanguageAndRequestContact = botService.getAdminInfo(update);
                SendMessage sendMessage = setUserLanguageAndRequestContact.join();
                try {
                    CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                    executeFuture.join();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            } else if ("Список Админов".equals(messageText) || "Adminlar ro'yxati".equals(messageText) && (isAdmin || isSuperAdmin)) {

                CompletableFuture<SendMessage> setUserLanguageAndRequestContact = buttonService.getAdminList(update);
                SendMessage sendMessage = setUserLanguageAndRequestContact.join();
                try {
                    CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                    executeFuture.join();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            } else if ((messageText.startsWith("SUPER_ADMIN - 998") || messageText.startsWith("ADMIN - 998")) && (isAdmin || isSuperAdmin)) {

                if (isAdmin) {

                    if (userLanguage.equals("UZ"))
                        sendTextMessage(chatId, "ADMIN roli boshqa adminlar haqida to'liq ma'lumot olish huquqiga ega emas ‼️");
                    else
                        sendTextMessage(chatId, "Роль ADMIN не имеет полного доступа к информацию другим администраторам ‼️");

                } else if (userStage.equals("ADMIN_SELECTED_FOR_DELETING")) {

                    CompletableFuture<SendMessage> setUserLanguageAndRequestContact = botService.deleteAdmin(update);
                    SendMessage sendMessage = setUserLanguageAndRequestContact.join();
                    try {
                        CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                    try {
                                        execute(sendMessage);
                                    } catch (TelegramApiException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                        );
                        executeFuture.join();

                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                } else {
                    CompletableFuture<SendMessage> setUserLanguageAndRequestContact = buttonService.adminInfoForSUPER_ADMIN(update);
                    SendMessage sendMessage = setUserLanguageAndRequestContact.join();
                    try {
                        CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                    try {
                                        execute(sendMessage);
                                    } catch (TelegramApiException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                        );
                        executeFuture.join();

                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            } else if (userStage.equals("ENTER_PHONE_NUMBER_FOR_CREATING_ADMIN") && isSuperAdmin) {

                CompletableFuture<SendMessage> sendMessageCompletableFuture = botService.createAdmin(update);
                SendMessage sendMessage = sendMessageCompletableFuture.join();
                try {
                    CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                    executeFuture.join();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            } else if (("Добавить Админ".equals(messageText) || "Admin qo'shish".equals(messageText)) && (isAdmin || isSuperAdmin)) {

                if (isAdmin) {
                    if (userLanguage.equals("UZ"))
                        sendTextMessage(chatId, "ADMIN roli boshqa admin qo'shish huquqiga ega emas ‼️");
                    else
                        sendTextMessage(chatId, "Роль АДМИН не имеет права добавлять еще одного админа ‼️");
                } else {

                    CompletableFuture<SendMessage> sendMessageCompletableFuture = buttonService.askPhoneNumberForAddingAdmin(update);
                    SendMessage sendMessage = sendMessageCompletableFuture.join();
                    try {
                        CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                    try {
                                        execute(sendMessage);
                                    } catch (TelegramApiException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                        );
                        executeFuture.join();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }

            } else if (("Admin o'chirish".equals(messageText) || "Удалить Админ".equals(messageText)) && isSuperAdmin) {

                CompletableFuture<SendMessage> setUserLanguageAndRequestContact = buttonService.askSelectAdminForDeleting(update);
                SendMessage sendMessage = setUserLanguageAndRequestContact.join();
                try {
                    CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                    executeFuture.join();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            } else if (isSuperAdmin || isAdmin) {

                CompletableFuture<SendMessage> setUserLanguageAndRequestContact = buttonService.superAdminButtons(update);
                SendMessage sendMessage = setUserLanguageAndRequestContact.join();
                try {
                    CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                    executeFuture.join();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            } else if (isUser) {

                CompletableFuture<SendMessage> setUserLanguageAndRequestContact = buttonService.userButtons(update);
                SendMessage sendMessage = setUserLanguageAndRequestContact.join();
                try {
                    CompletableFuture<Void> executeFuture = CompletableFuture.runAsync(() -> {
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                    executeFuture.join();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void sendTextMessage(Long chatId, String text) {

        SendMessage message = new SendMessage(chatId.toString(), text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendDocument(Long chatId, String caption, InputFile inputFile) {
        final var sendDocument = SendDocument.builder()
                .chatId(chatId)
                .document(inputFile)
                .caption(caption)
                .build();
        try {
            execute(sendDocument);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendPhoto(Long chatId, String photoCaption, InputFile inputFile) {
        final var sendPhoto = SendPhoto.builder()
                .chatId(chatId)
                .photo(inputFile)
                .caption(photoCaption)
                .build();
        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendBirthdayMessageToColleagues(Employee babyEmployee, List<Long> colleaguesChatIds) {

        SendMessage message = new SendMessage();
        final var employeeFullName = babyEmployee.getFullName();

        String text_uz = "Assalomu Alaykum! 🎉" +
                "\nBugun sizning hamkasbingiz " + employeeFullName + " o'zlarining " + (babyEmployee.getAge() + 1) + " yoshlarini qarshi olyabdilar! 🎂🥳" +
                "\nSizga va hamkasbingizga omad va muvaffaqiyat doim hamroh bo'lsin! 🎉" +
                "\nDo'stona muhitni bardavom qilish maqsadida siz ham ularni tabriklashni unutmang. 😊";

        String text_ru = "Добрый день! 🎉\n" +
                "Сегодня ваш коллега " + employeeFullName + " отмечает свое " + (babyEmployee.getAge() + 1) + "-летие! 🎂🥳\n" +
                "Пусть удача и успех всегда сопутствуют Вам и Вашему коллеге! 🎉\n" +
                "Не забудьте поздравить его, чтобы поддержать дружескую атмосферу. 😊";

        for (Long colleaguesChatId : colleaguesChatIds) {
            message.setChatId(colleaguesChatId);
            userLanguage = userRepository.getUserLanguageByUserChatId(colleaguesChatId);
            message.setText(userLanguage.equals("UZ") ? text_uz : text_ru);

            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendCongratulation(Employee babyEmployee, Long babyEmployeeChatId) {

        String text_uz = "Assalomu Alaykum! 🎉\n" +
                "Hurmatli " + babyEmployee.getFullName() + ", bugun siz qarshi olayotgan " + (babyEmployee.getAge() + 1) + " yoshingiz bilan qutlaymiz! 🎂🥳\n" +
                "MKBankning yanada yuqori cho'qqilarga chiqishida o'z hissangizni qo'shayotganingizdan mamnunmiz.\n" +
                "Sizga uzoq umr, sog'lik, salomatlik va baxt-saodat tilaymiz! 🎉";

        String text_ru = "Добрый день! 🎉\n" +
                "Дорогой " + babyEmployee.getFullName() + ", сегодня мы поздравляем тебя с " + (babyEmployee.getAge() + 1) + "-летием! 🎂🥳\n" +
                "Мы рады, что вы вносите свой вклад в рост МКБанка до более высоких высот.\n" +
                "Желаем вам долгих лет жизни, здоровья, благополучия и счастья! 🎉";

        userLanguage = userRepository.getUserLanguageByUserChatId(babyEmployeeChatId);
        SendMessage message = new SendMessage();
        message.setChatId(babyEmployeeChatId);
        message.setText(userLanguage.equals("UZ") ? text_uz : text_ru);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Autowired
    public void setService(BotService service) {
        EmployeeBot.botService = service;
    }

    @Autowired
    public void setButtonService(ButtonService service) {
        EmployeeBot.buttonService = service;
    }

    @Autowired
    public void setFileService(FileService service) {
        EmployeeBot.fileService = service;
    }

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        EmployeeBot.userRepository = userRepository;
    }

    @Autowired
    public void setEmployeeRepository(EmployeeRepository employeeRepository) {
        EmployeeBot.employeeRepository = employeeRepository;
    }

    @Autowired
    public void setDepartmentRepository(DepartmentRepository departmentRepository) {
        EmployeeBot.departmentRepository = departmentRepository;
    }

    @Autowired
    public void setManagementRepository(ManagementRepository managementRepository) {
        EmployeeBot.managementRepository = managementRepository;
    }

    @Autowired
    public void setPositionRepository(PositionRepository positionRepository) {
        EmployeeBot.positionRepository = positionRepository;
    }

    @Override
    public String getBotUsername() {
        return this.botUsername;
    }

    @Override
    public String getBotToken() {
        return this.botToken;
    }


}