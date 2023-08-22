package com.example.mkb_employee_bot.component.bot;

import com.example.mkb_employee_bot.entity.Department;
import com.example.mkb_employee_bot.entity.Management;
import com.example.mkb_employee_bot.entity.enums.Stage;
import com.example.mkb_employee_bot.repository.DepartmentRepository;
import com.example.mkb_employee_bot.repository.EmployeeRepository;
import com.example.mkb_employee_bot.repository.ManagementRepository;
import lombok.Data;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import com.example.mkb_employee_bot.service.ButtonService;
import com.example.mkb_employee_bot.service.BotServiceImpl;
import com.example.mkb_employee_bot.repository.UserRepository;

import java.util.concurrent.CompletableFuture;

@Data
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmployeeBot extends TelegramLongPollingBot {

    private static BotServiceImpl botService;
    private static ButtonService buttonService;

    private static UserRepository userRepository;
    private static EmployeeRepository employeeRepository;
    private static DepartmentRepository departmentRepository;
    private static ManagementRepository managementRepository;

    Long chatId;
    String userStage;
    String userLanguage;
    Department selectedDepartment = new Department();
    Department prevDepartment = new Department();
    Management prevManagement = new Management();
    String botUsername = "mkb_employees_bot";
    String botToken = "6608186289:AAER7qqqE-mNPMZCZrIj6zm8JS_q7o7eCmw";
    String welcomeMessage = """
            MKBank Xodimlari botiga xush kelibsiz!
                        
            Добро пожаловать в бот для сотрудников МКБанк!
            """;

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage()) {

            chatId = update.getMessage().getChatId();
            userLanguage = botService.getUserLanguage(chatId);
            userStage = userRepository.getUserStageByUserChatId(chatId);

            final var userRole = botService.getUserRole(chatId);
            final var isSuperAdmin = userRole.equals("SUPER_ADMIN");
            final var isAdmin = userRole.equals("ADMIN");
            final var isUser = userRole.equals("USER");

            Message message = update.getMessage();
            String messageText = message.getText() == null ? "" : message.getText();
            System.out.println("userStage: " + userStage);
            System.out.println("messageText: " + messageText);

            final var caseContainingList = employeeRepository.findByFullNameIgnoreCaseContaining(messageText);
            final var isCaseContainingListEmpty = caseContainingList.isEmpty();

            final var messageSection = botService.getMessageSection(messageText);

            if (message.hasContact()) {
                CompletableFuture<Void> updateContactFuture = CompletableFuture.runAsync(() -> botService.setPhoneNumber(update));
                updateContactFuture.join();
                userRepository.updateUserStageByUserChatId(chatId, Stage.CONTACT_SHARED.name());

                if (userLanguage.equals("UZ") && !userRole.equals("USER"))
                    sendTextMessage(chatId.toString(), "Sizning rolingiz: " + userRole);
                else if (userLanguage.equals("RU") && !userRole.equals("USER"))
                    sendTextMessage(chatId.toString(), "Ваш роль: " + userRole);
            }

            if ("/start".equals(messageText)) {

                sendTextMessage(String.valueOf(chatId), welcomeMessage);
                CompletableFuture<SendMessage> welcomeMessage = buttonService.selectLanguageButtons(update);
                SendMessage sendMessage = welcomeMessage.join();
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
                    });
                    executeFuture.join();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            } else if ("Bosh Menu".equals(messageText) || "Главное Меню".equals(messageText)) {
                CompletableFuture<SendMessage> messageCompletableFuture;
                if (isAdmin || isSuperAdmin) messageCompletableFuture = buttonService.superAdminButtons(update);
                else messageCompletableFuture = buttonService.userButtons(update);

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
                    });
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
                    });
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
                    });
                    executeFuture.join();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else if ((("Сотрудник".equals(messageText) || "Xodim".equals(messageText)) && isUser) || ("Найти сотрудника".equals(messageText) || "Xodimni qidirish".equals(messageText)) && (isAdmin || isSuperAdmin)) {

                CompletableFuture<SendMessage> sendMessageCompletableFuture = buttonService.employeeSectionUserRoleButtons(update);
                SendMessage sendMessage = sendMessageCompletableFuture.join();
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
                            sendTextMessage(String.valueOf(chatId), "Iltimos, ro'yhatdagi bo'limlardan birini tanlang");
                        else sendTextMessage(chatId.toString(), "Пожалуйста, выберите один из разделов списка");
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
                    });
                    executeFuture.join();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else if (!isCaseContainingListEmpty && isUser && (userStage.equals("ENTERED_EMPLOYEE_NAME_FOR_SEARCH_ROLE_USER"))) {

                CompletableFuture<SendMessage> sendMessageCompletableFuture = buttonService.findEmployeeSectionUserRoleButtons(update);
                SendMessage sendMessage = sendMessageCompletableFuture.join();
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
            } else if (isUser && userStage.equals("SELECTED_EMPLOYEE_NAME_FOR_SEARCH_ROLE_USER")) {

                CompletableFuture<SendMessage> sendMessageCompletableFuture = botService.getSelectedEmployeeInfo(update);
                SendMessage sendMessage = sendMessageCompletableFuture.join();
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
                    });
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
                    });
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
                    });
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
                    });
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
                    });
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
                    });
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
                    });
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
                    });
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
                    });
                    executeFuture.join();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            } else if ("Добавить Должность".equals(messageText) || "Lavozim qo'shish".equals(messageText) && (isAdmin || isSuperAdmin)) {

                CompletableFuture<SendMessage> messageCompletableFuture = buttonService.askSelectManagementForCreatingPosition(update);
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

                prevManagement = managementRepository.findByName(messageText).get();
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
                    });
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
                    });
                    executeFuture.join();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            } else if (userStage.equals("DEPARTMENT_SELECTED_FOR_UPDATING_MANAGEMENT") && (isAdmin || isSuperAdmin)) {

                prevDepartment = departmentRepository.findByName(messageText).get();
                CompletableFuture<SendMessage> messageCompletableFuture = buttonService.askSelectManagementForUpdating(prevDepartment, update);
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
                    });
                    executeFuture.join();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            } else if (userStage.equals("DEPARTMENT_SELECTED_FOR_SAVING_UPDATED_MANAGEMENT") && (isAdmin || isSuperAdmin)) {

                selectedDepartment = departmentRepository.findByName(messageText).get();
                CompletableFuture<SendMessage> messageCompletableFuture = buttonService.askingNameForCreatingManagement(update, "forSaving");
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
                    });
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
                    });
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
                    });
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
                    });
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
                    });
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
                    });
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
                    });
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
                    });
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
                    });
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
                    });
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
    public void setService(BotServiceImpl service) {
        EmployeeBot.botService = service;
    }

    @Autowired
    public void setButtonService(ButtonService service) {
        EmployeeBot.buttonService = service;
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

    @Override
    public String getBotUsername() {
        return this.botUsername;
    }

    @Override
    public String getBotToken() {
        return this.botToken;
    }
}