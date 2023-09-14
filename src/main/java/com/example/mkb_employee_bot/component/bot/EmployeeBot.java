package com.example.mkb_employee_bot.component.bot;

import com.example.mkb_employee_bot.entity.enums.*;
import com.example.mkb_employee_bot.service.FileService;
import jakarta.ws.rs.NotFoundException;
import lombok.Data;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import com.example.mkb_employee_bot.entity.*;
import com.example.mkb_employee_bot.repository.*;
import com.example.mkb_employee_bot.service.BotService;
import com.example.mkb_employee_bot.service.ButtonService;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

@Data
@Component
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmployeeBot extends TelegramLongPollingBot {

    private static BotService botService;
    private static ButtonService buttonService;
    private static FileService fileService;

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

    Department prevDepartment = new Department();
    Department selectedDepartment = new Department();

    Education education = new Education();
    Management prevManagement = new Management();

    Position prevPosition = new Position();
    Position selectedPosition = new Position();

    Employee deletingEmployee = new Employee();
    Employee updatingEmployee = new Employee();
    Employee creatingEmployee = new Employee();

    String botUsername = "mkb_employees_bot";
    String botToken = "6608186289:AAER7qqqE-mNPMZCZrIj6zm8JS_q7o7eCmw";

//    @Value("${token}")
//    private String botToken;
//
//    @Value("${username}")
//    private String botUsername;

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage()) {

            chatId = update.getMessage().getChatId();
            userLanguage = botService.getUserLanguage(chatId);
            userStage = userRepository.getUserStageByUserChatId(chatId);
            String userStepByUserChatId = userRepository.getUserStepByUserChatId(chatId);
            userStep = userStepByUserChatId == null ? "" : userStepByUserChatId;

            final var userRole = botService.getUserRole(chatId);
            final var isSuperAdmin = userRole.equals("SUPER_ADMIN");
            final var isAdmin = userRole.equals("ADMIN");
            final var isUser = userRole.equals("USER");

            Message message = update.getMessage();
            String messageText = message.getText() == null ? "" : message.getText();
            System.out.println("userStage: " + userStage);
            System.out.println("messageText: " + messageText);
            System.out.println("userStep: " + userStep);

            final var caseContainingList = employeeRepository.findByFullNameIgnoreCaseContaining(messageText);
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

                sendTextMessage(String.valueOf(chatId), welcomeMessage);
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
                            sendTextMessage(String.valueOf(chatId), "Iltimos, ro'yhatdagi bo'limlardan birini tanlang ❗️");
                        else sendTextMessage(chatId.toString(), "Пожалуйста, выберите один из разделов списка ❗️");
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

                CompletableFuture<SendMessage> sendMessageCompletableFuture = botService.getSelectedEmployeeInfo(update);
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
                        sendTextMessage(chatId.toString(), "Информация о сотруднике изменена ❗️");
                    else
                        sendTextMessage(chatId.toString(), "Xodim ma'lumotlari o'zgartirildi ❗️");
                }

            } else if (userStage.equals("SELECTED_EMPLOYEE_UPDATING_INFO_ROLE_ADMIN") && (isAdmin || isSuperAdmin)) {

                CompletableFuture<SendMessage> setUserLanguageAndRequestContact = buttonService.askInfoForSelectedSection(update);
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

                updatingEmployee = employeeRepository.findByFullName(messageText).orElseThrow();
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
                    sendTextMessage(chatId.toString(), "Начать редактирование ⁉️");
                else
                    sendTextMessage(chatId.toString(), "Tahrirlashni boshlaysizmi ⁉️");

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
                    sendTextMessage(chatId.toString(), "Jarayon to'xtatildi❗️");
                else
                    sendTextMessage(chatId.toString(), "Процесс остановлен❗️");
                buttonService.retryUserSteps();

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

                if (userLanguage.equals("UZ"))
                    sendTextMessage(chatId.toString(), "Saqlash uchun xodimning ma'lumotlarini tasdiqlaysizmi? ⬇️");
                else
                    sendTextMessage(chatId.toString(), "Подтвердите информацию о сотруднике для сохранения? ⬇️");

                CompletableFuture<SendMessage> setUserLanguageAndRequestContact = buttonService.completeAddingEmployeeInfo(update, creatingEmployee);
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

            } else if ((Stage.SELECTED_EMPLOYEE_FILE_TYPE.name().equals(userStep)) || ("Tasdiqlash ✅".equals(messageText) || "Потвердить ✅".equals(messageText))) {

                CompletableFuture<SendMessage> sendMessageCompletableFuture = new CompletableFuture<>();

                if (("Tasdiqlash ✅".equals(messageText) || "Потвердить ✅".equals(messageText))) {
                    sendMessageCompletableFuture = botService.createEmployee(creatingEmployee, update);
                    creatingEmployee = new Employee();

                } else if (("Bekor qilish ❌".equals(messageText) || "Отменить ❌".equals(messageText))) {
                    sendMessageCompletableFuture = buttonService.cancelledConfirmation(update, "forCreatingEmployee");
                    creatingEmployee = new Employee();

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
                    sendTextMessage(chatId.toString(), "Подтвердить сохранение ️⁉️");
                else
                    sendTextMessage(chatId.toString(), "Saqlashni tasdiqlaysizmi ⁉️");

            } else if ((userStage.equals("POSITION_FOR_CREATING_EMPLOYEE") || !userStep.equals("")) && (isAdmin || isSuperAdmin)) {

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
                else if (Stage.SELECTED_EMPLOYEE_EDUCATION_TYPE.name().equals(userStep)) {
                    education.setType(EduType.valueOf(messageText));
                } else if (Stage.ENTERED_EMPLOYEE_EDUCATION_NAME.name().equals(userStep))
                    education.setName(messageText);
                else if (Stage.ENTERED_EMPLOYEE_EDUCATION_FIELD.name().equals(userStep))
                    education.setEducationField(messageText);
                else if (Stage.ENTERED_EMPLOYEE_EDUCATION_PERIOD.name().equals(userStep)) {
                    String[] dateFromPeriod = buttonService.getDateFromPeriod(messageText);
                    education.setStartedDate(dateFromPeriod[0]);
                    education.setEndDate(dateFromPeriod[1]);
                    creatingEmployee.setEducations(List.of(education));
                } else if (Stage.ENTERED_EMPLOYEE_SKILLS.name().equals(userStep)) {

                    List<Skill> skillList = new ArrayList<>();
                    for (String s : buttonService.splitSkills(messageText)) {
                        Skill skill = new Skill();
                        skill.setName(s);
                        skillList.add(skill);
                    }
                    creatingEmployee.setSkills(skillList);
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

            } else if (userStage.equals("CONFIRMATION_FOR_DELETING_EMPLOYEE") && (isAdmin || isSuperAdmin)) {
                CompletableFuture<SendMessage> sendMessageCompletableFuture = new CompletableFuture<>();

                if ("Tasdiqlash ✅".equals(messageText) || "Потвердить ✅".equals(messageText))
                    sendMessageCompletableFuture = botService.deleteEmployee(deletingEmployee, update);

                else if ("Bekor qilish ❌".equals(messageText) || "Отменить ❌".equals(messageText))
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

                deletingEmployee = employeeRepository.findByFullName(messageText).orElseThrow();
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
                    sendTextMessage(chatId.toString(), "Подтвердить удаление ️");
                else
                    sendTextMessage(chatId.toString(), "O'chirishni tasdiqlaysizmi ⁉️");

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
                sendTextMessage(chatId.toString(), text);

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

                sendTextMessage(chatId.toString(), text);

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
                        sendTextMessage(chatId.toString(), "ADMIN roli boshqa adminlar haqida to'liq ma'lumot olish huquqiga ega emas ‼️");
                    else
                        sendTextMessage(chatId.toString(), "Роль ADMIN не имеет полного доступа к информацию другим администраторам ‼️");

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
                        sendTextMessage(chatId.toString(), "ADMIN roli boshqa admin qo'shish huquqiga ega emas ‼️");
                    else
                        sendTextMessage(chatId.toString(), "Роль АДМИН не имеет права добавлять еще одного админа ‼️");
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

    private void sendTextMessage(String chatId, String text) {
        SendMessage message = new SendMessage(chatId, text);
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