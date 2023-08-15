package com.example.mkb_employee_bot.service;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import com.example.mkb_employee_bot.repository.*;
import com.example.mkb_employee_bot.entiry.Employee;
import com.example.mkb_employee_bot.entiry.enums.Stage;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;

@Service
@RequiredArgsConstructor
public class ButtonService {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final PositionRepository positionRepository;
    private final DepartmentRepository departmentRepository;
    private final ManagementRepository managementRepository;

    private String back = "";
    private String mainMenu = "";
    private String returnText = "";
    private final String sighDown = "⬇\uFE0F";
    private final String sighBack = "⬅\uFE0F";

    public CompletableFuture<SendMessage> selectLanguageButtons(Update update) {

        return CompletableFuture.supplyAsync(() -> {
                    ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
                    List<KeyboardRow> keyboardRowList = new ArrayList<>();
                    replyKeyboardMarkup.setSelective(true);
                    replyKeyboardMarkup.setOneTimeKeyboard(true);
                    replyKeyboardMarkup.setResizeKeyboard(true);
                    keyboardRowList.add(
                            new KeyboardRow(List.of(
                                    KeyboardButton.builder()
                                            .text("\uD83C\uDDFA\uD83C\uDDFF")
                                            .build(),
                                    KeyboardButton.builder()
                                            .text("\uD83C\uDDF7\uD83C\uDDFA")
                                            .build()
                            ))
                    );
                    returnText = """
                            Iltimos, botdan foydalanish uchun tilni tanlang\uD83C\uDDFA\uD83C\uDDFF\s

                            Пожалуйста, выберите язык для использования бота\uD83C\uDDF7\uD83C\uDDFA\s""";

                    final var chatId = update.getMessage().getChatId();
                    replyKeyboardMarkup.setKeyboard(keyboardRowList);

                    return SendMessage.builder()
                            .replyMarkup(replyKeyboardMarkup)
                            .chatId(String.valueOf(chatId))
                            .text(returnText)
                            .build();
                }
        );
    }

    /***
     * USER role
     */
    public CompletableFuture<SendMessage> userButtons(Update update) {
        return CompletableFuture.supplyAsync(() -> {

            final var chatId = update.getMessage().getChatId();
            final var userLanguage = getUserLanguage(chatId);
            String button1, button2, button3, button4;

            if (userLanguage.equals("RU")) {
                returnText = "Выберите нужный раздел для получения информации " + sighDown;
                button1 = "Сотрудник";
                button2 = "Должности";
                button3 = "Департаменты";
                button4 = "Отделы";
            } else {
                returnText = "Ma'lumot olish uchun kerakli bo'limni tanlang " + sighDown;
                button1 = "Xodim";
                button2 = "Lavozimlar";
                button3 = "Departamentlar";
                button4 = "Boshqarmalar";
            }

            ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
            replyKeyboardMarkup.setSelective(true);
            replyKeyboardMarkup.setResizeKeyboard(true);
            replyKeyboardMarkup.setOneTimeKeyboard(true);
            List<KeyboardRow> keyboardRowList = new ArrayList<>();

            keyboardRowList.add(
                    new KeyboardRow(List.of(
                            KeyboardButton.builder()
                                    .text(button1)
                                    .build(),
                            KeyboardButton.builder()
                                    .text(button2)
                                    .build()
                    ))
            );
            keyboardRowList.add(
                    new KeyboardRow(List.of(
                            KeyboardButton.builder()
                                    .text(button3)
                                    .build(),
                            KeyboardButton.builder()
                                    .text(button4)
                                    .build()
                    ))
            );
            replyKeyboardMarkup.setKeyboard(keyboardRowList);

            return SendMessage.builder()
                    .replyMarkup(replyKeyboardMarkup)
                    .chatId(String.valueOf(chatId))
                    .text(returnText)
                    .build();
        });
    }

    /***
     * USER role
     */
    public CompletableFuture<SendMessage> positionEmployees(Update update) {
        return CompletableFuture.supplyAsync(() -> {

            final var chatId = update.getMessage().getChatId();
            final var userLanguage = getUserLanguage(chatId);
            final var department = positionRepository.findByName(update.getMessage().getText()).orElseThrow(NotFoundException::new);
            final var managementEmployees = getManagementEmployees(department.getId());

            if (userLanguage.equals("RU")) {
                if (managementEmployees.isEmpty())
                    returnText = "Список пустой, сотрудников на данной Должности нет в списке.";
                else
                    returnText = "Выберите нужного Сотрудника из списка " + sighDown;
                mainMenu = "Главное Меню";
            } else {
                if (managementEmployees.isEmpty())
                    returnText = "Ro'yhat bo'sh, ushbu Bo'limdagi xodimlar ro'yxatda yo'q.";
                else
                    returnText = "Ro'yxatdan kerakli xodimni tanlang " + sighDown;
                mainMenu = "Bosh Menu";
            }

            userRepository.updateUserStageByUserChatId(chatId, Stage.POSITION_SELECTED_FOR_EMPLOYEE_INFO.name());
            ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
            List<KeyboardRow> keyboardRowList = new ArrayList<>();
            replyKeyboardMarkup.setSelective(true);
            replyKeyboardMarkup.setResizeKeyboard(true);
            replyKeyboardMarkup.setOneTimeKeyboard(true);

            for (Employee employee : managementEmployees) {
                keyboardRowList.add(
                        new KeyboardRow(
                                Collections.singletonList(
                                        KeyboardButton.builder()
                                                .text(employee.getFullName())
                                                .build()
                                )
                        )
                );
            }

            keyboardRowList.add(
                    new KeyboardRow(List.of(
                            KeyboardButton.builder()
                                    .text(mainMenu)
                                    .build()
                    ))
            );
            replyKeyboardMarkup.setKeyboard(keyboardRowList);

            return SendMessage.builder()
                    .replyMarkup(replyKeyboardMarkup)
                    .chatId(String.valueOf(chatId))
                    .text(returnText)
                    .build();
        });
    }

    /***
     * USER role
     */
    public CompletableFuture<SendMessage> managementEmployees(Update update) {
        return CompletableFuture.supplyAsync(() -> {

            final var chatId = update.getMessage().getChatId();
            final var userLanguage = getUserLanguage(chatId);
            final var department = managementRepository.findByName(update.getMessage().getText()).orElseThrow(NotFoundException::new);
            final var managementEmployees = getManagementEmployees(department.getId());

            if (userLanguage.equals("RU")) {
                if (managementEmployees.isEmpty())
                    returnText = "Список пустой, Сотрудников этого Отдела нет в списке.";
                else
                    returnText = "Выберите нужного Сотрудника из списка " + sighDown;
                mainMenu = "Главное Меню";
            } else {
                if (managementEmployees.isEmpty())
                    returnText = "Ro'yhat bo'sh, ushbu Bo'limdagi xodimlar ro'yxatda yo'q.";
                else
                    returnText = "Ro'yxatdan kerakli xodimni tanlang " + sighDown;
                mainMenu = "Bosh Menu";
            }

            userRepository.updateUserStageByUserChatId(chatId, Stage.MANAGEMENT_SELECTED_FOR_EMPLOYEE_INFO.name());
            ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
            List<KeyboardRow> keyboardRowList = new ArrayList<>();
            replyKeyboardMarkup.setSelective(true);
            replyKeyboardMarkup.setResizeKeyboard(true);
            replyKeyboardMarkup.setOneTimeKeyboard(true);

            for (Employee employee : managementEmployees) {
                keyboardRowList.add(
                        new KeyboardRow(
                                Collections.singletonList(
                                        KeyboardButton.builder()
                                                .text(employee.getFullName())
                                                .build()
                                )
                        )
                );
            }

            keyboardRowList.add(
                    new KeyboardRow(List.of(
                            KeyboardButton.builder()
                                    .text(mainMenu)
                                    .build()
                    ))
            );
            replyKeyboardMarkup.setKeyboard(keyboardRowList);

            return SendMessage.builder()
                    .replyMarkup(replyKeyboardMarkup)
                    .chatId(String.valueOf(chatId))
                    .text(returnText)
                    .build();
        });
    }

    /***
     * USER role
     */
    public CompletableFuture<SendMessage> departmentEmployees(Update update) {
        return CompletableFuture.supplyAsync(() -> {

            final var chatId = update.getMessage().getChatId();
            final var userLanguage = getUserLanguage(chatId);
            final var department = departmentRepository.findByName(update.getMessage().getText()).orElseThrow(NotFoundException::new);
            final var departmentEmployees = getDepartmentEmployees(department.getId());

            if (userLanguage.equals("RU")) {
                if (departmentEmployees.isEmpty())
                    returnText = "Список пустой, Сотрудников этого Департамента нет в списке.";
                else
                    returnText = "Выберите нужного Сотрудника из списка " + sighDown;
                mainMenu = "Главное Меню";
            } else {
                if (departmentEmployees.isEmpty())
                    returnText = "Ro'yhat bo'sh, ushbu Departamentdagi xodimlar ro'yxatda yo'q.";
                else
                    returnText = "Ro'yxatdan kerakli xodimni tanlang " + sighDown;
                mainMenu = "Bosh Menu";
            }

            userRepository.updateUserStageByUserChatId(chatId, Stage.DEPARTMENT_SELECTED_FOR_EMPLOYEE_INFO.name());
            ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
            List<KeyboardRow> keyboardRowList = new ArrayList<>();
            replyKeyboardMarkup.setSelective(true);
            replyKeyboardMarkup.setResizeKeyboard(true);
            replyKeyboardMarkup.setOneTimeKeyboard(true);

            for (Employee departmentEmployee : departmentEmployees) {
                keyboardRowList.add(
                        new KeyboardRow(
                                Collections.singletonList(
                                        KeyboardButton.builder()
                                                .text(departmentEmployee.getFullName())
                                                .build()
                                )
                        )
                );
            }

            keyboardRowList.add(
                    new KeyboardRow(List.of(
                            KeyboardButton.builder()
                                    .text(mainMenu)
                                    .build()
                    ))
            );
            replyKeyboardMarkup.setKeyboard(keyboardRowList);

            return SendMessage.builder()
                    .replyMarkup(replyKeyboardMarkup)
                    .chatId(String.valueOf(chatId))
                    .text(returnText)
                    .build();
        });
    }

    /***
     * USER role
     */
    public CompletableFuture<SendMessage> departmentSectionUserRoleButtons(Update update) {
        return CompletableFuture.supplyAsync(() -> {

            final var chatId = update.getMessage().getChatId();
            final var userLanguage = getUserLanguage(chatId);
            final var departmentNames = getDepartmentNames();

            if (userLanguage.equals("RU")) {
                if (departmentNames.isEmpty())
                    returnText = "Список пустой, Департаментов нет";
                else
                    returnText = "Выберите нужный Департамент из списка " + sighDown;
                mainMenu = "Главное Меню";
            } else {
                if (departmentNames.isEmpty())
                    returnText = "Ro'yxat bo'sh, Departamentlar yo'q";
                else
                    returnText = "Ro'yxatdan kerakli Departamentni tanlang " + sighDown;
                mainMenu = "Bosh Menu";
            }

            userRepository.updateUserStageByUserChatId(chatId, Stage.SECTION_SELECTED.name());
            ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
            List<KeyboardRow> keyboardRowList = new ArrayList<>();
            replyKeyboardMarkup.setSelective(true);
            replyKeyboardMarkup.setResizeKeyboard(true);
            replyKeyboardMarkup.setOneTimeKeyboard(true);

            for (String departmentName : departmentNames) {
                keyboardRowList.add(
                        new KeyboardRow(
                                Collections.singletonList(
                                        KeyboardButton.builder()
                                                .text(departmentName)
                                                .build()
                                )
                        )
                );
            }

            keyboardRowList.add(
                    new KeyboardRow(List.of(
                            KeyboardButton.builder()
                                    .text(mainMenu)
                                    .build()
                    ))
            );
            replyKeyboardMarkup.setKeyboard(keyboardRowList);

            return SendMessage.builder()
                    .replyMarkup(replyKeyboardMarkup)
                    .chatId(String.valueOf(chatId))
                    .text(returnText)
                    .build();
        });
    }

    /***
     * USER role
     */
    public CompletableFuture<SendMessage> managementSectionUserRoleButtons(Update update) {
        return CompletableFuture.supplyAsync(() -> {

            final var chatId = update.getMessage().getChatId();
            final var userLanguage = getUserLanguage(chatId);
            final var managementNames = getManagementNames();

            if (userLanguage.equals("RU")) {
                if (managementNames.isEmpty())
                    returnText = "Список пустой, отделов нет";
                else
                    returnText = "Выберите нужный Департамент из списка " + sighDown;
                mainMenu = "Главное Меню";
            } else {
                if (managementNames.isEmpty())
                    returnText = "Ro'yxat bo'sh, Boshqarmalar yo'q";
                else
                    returnText = "Ro'yxatdan kerakli Departamentni tanlang " + sighDown;
                mainMenu = "Bosh Menu";
            }

            userRepository.updateUserStageByUserChatId(chatId, Stage.SECTION_SELECTED.name());
            ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
            List<KeyboardRow> keyboardRowList = new ArrayList<>();
            replyKeyboardMarkup.setSelective(true);
            replyKeyboardMarkup.setResizeKeyboard(true);
            replyKeyboardMarkup.setOneTimeKeyboard(true);

            for (String managementName : managementNames) {
                keyboardRowList.add(
                        new KeyboardRow(
                                Collections.singletonList(
                                        KeyboardButton.builder()
                                                .text(managementName)
                                                .build()
                                )
                        )
                );
            }

            keyboardRowList.add(
                    new KeyboardRow(List.of(
                            KeyboardButton.builder()
                                    .text(mainMenu)
                                    .build()
                    ))
            );
            replyKeyboardMarkup.setKeyboard(keyboardRowList);

            return SendMessage.builder()
                    .replyMarkup(replyKeyboardMarkup)
                    .chatId(String.valueOf(chatId))
                    .text(returnText)
                    .build();
        });
    }

    /***
     * USER role
     */
    public CompletableFuture<SendMessage> positionSectionUserRoleButtons(Update update) {
        return CompletableFuture.supplyAsync(() -> {

            final var chatId = update.getMessage().getChatId();
            final var userLanguage = getUserLanguage(chatId);
            final var positionNames = getPositionNames();

            if (userLanguage.equals("RU")) {
                if (positionNames.isEmpty())
                    returnText = "Список пустой, Должности нет";
                else
                    returnText = "Выберите нужный Департамент из списка " + sighDown;
                mainMenu = "Главное Меню";
            } else {
                if (positionNames.isEmpty())
                    returnText = "Ro'yxat bo'sh, Lavozimlar yo'q";
                else
                    returnText = "Ro'yxatdan kerakli Departamentni tanlang " + sighDown;
                mainMenu = "Bosh Menu";
            }

            userRepository.updateUserStageByUserChatId(chatId, Stage.SECTION_SELECTED.name());
            ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
            List<KeyboardRow> keyboardRowList = new ArrayList<>();
            replyKeyboardMarkup.setSelective(true);
            replyKeyboardMarkup.setResizeKeyboard(true);
            replyKeyboardMarkup.setOneTimeKeyboard(true);

            for (String positionName : positionNames) {
                keyboardRowList.add(
                        new KeyboardRow(
                                Collections.singletonList(
                                        KeyboardButton.builder()
                                                .text(positionName)
                                                .build()
                                )
                        )
                );
            }

            keyboardRowList.add(
                    new KeyboardRow(List.of(
                            KeyboardButton.builder()
                                    .text(mainMenu)
                                    .build()
                    ))
            );
            replyKeyboardMarkup.setKeyboard(keyboardRowList);

            return SendMessage.builder()
                    .replyMarkup(replyKeyboardMarkup)
                    .chatId(String.valueOf(chatId))
                    .text(returnText)
                    .build();
        });
    }

    /***
     * USER role
     */
    public CompletableFuture<SendMessage> employeeSectionUserRoleButtons(Update update) {
        return CompletableFuture.supplyAsync(() -> {

            final var chatId = update.getMessage().getChatId();
            final var userLanguage = getUserLanguage(chatId);
            final var employees = getEmployees();

            if (userLanguage.equals("RU")) {
                if (employees.isEmpty())
                    returnText = "Список пустой, Сотрудники нет";
                else
                    returnText = "Введите имя и фамилию сотрудника " + sighDown;
                mainMenu = "Главное Меню";
            } else {
                if (employees.isEmpty())
                    returnText = "Ro'yxat bo'sh, Xodimlar yo'q";
                else
                    returnText = "Xodimning ism familiyasini kiriting " + sighDown;
                mainMenu = "Bosh Menu";
            }

            userRepository.updateUserStageByUserChatId(chatId, Stage.SECTION_SELECTED.name());
            ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
            List<KeyboardRow> keyboardRowList = new ArrayList<>();
            replyKeyboardMarkup.setSelective(true);
            replyKeyboardMarkup.setResizeKeyboard(true);
            replyKeyboardMarkup.setOneTimeKeyboard(true);

            keyboardRowList.add(
                    new KeyboardRow(List.of(
                            KeyboardButton.builder()
                                    .text(mainMenu)
                                    .build()
                    ))
            );
            replyKeyboardMarkup.setKeyboard(keyboardRowList);

            userRepository.updateUserStageByUserChatId(chatId, Stage.ENTERED_EMPLOYEE_NAME_FOR_SEARCH_ROLE_USER.name());
            return SendMessage.builder()
                    .replyMarkup(replyKeyboardMarkup)
                    .chatId(String.valueOf(chatId))
                    .text(returnText)
                    .build();
        });
    }

    /***
     * USER role
     */
    public CompletableFuture<SendMessage> findEmployeeSectionUserRoleButtons(Update update) {
        return CompletableFuture.supplyAsync(() -> {

            final var chatId = update.getMessage().getChatId();
            final var userLanguage = getUserLanguage(chatId);
            final var employees = employeeRepository.findByFullNameIgnoreCaseContaining(
                    update.getMessage().getText()
            );

            if (userLanguage.equals("RU")) {
                returnText = "Выберите нужного сотрудника из списка " + sighDown;
                mainMenu = "Главное Меню";
            } else {
                returnText = "Kerakli xodimni ro'yhatdan tanlang " + sighDown;
                mainMenu = "Bosh Menu";
            }

            ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
            List<KeyboardRow> keyboardRowList = new ArrayList<>();
            replyKeyboardMarkup.setSelective(true);
            replyKeyboardMarkup.setResizeKeyboard(true);
            replyKeyboardMarkup.setOneTimeKeyboard(true);

            for (Employee employee : employees) {
                final var fullName = employee.getFullName();

                keyboardRowList.add(
                        new KeyboardRow(
                                Collections.singletonList(
                                        KeyboardButton.builder()
                                                .text(fullName)
                                                .build()
                                )
                        )
                );
            }

            keyboardRowList.add(
                    new KeyboardRow(List.of(
                            KeyboardButton.builder()
                                    .text(mainMenu)
                                    .build()
                    ))
            );
            replyKeyboardMarkup.setKeyboard(keyboardRowList);

            userRepository.updateUserStageByUserChatId(chatId, Stage.SELECTED_EMPLOYEE_NAME_FOR_SEARCH_ROLE_USER.name());
            return SendMessage.builder()
                    .replyMarkup(replyKeyboardMarkup)
                    .chatId(String.valueOf(chatId))
                    .text(returnText)
                    .build();
        });
    }

    /***
     * SUPER_ADMIN role
     */
    public CompletableFuture<SendMessage> superAdminButtons(Update update) {
        return CompletableFuture.supplyAsync(() -> {

            final var chatId = update.getMessage().getChatId();
            final var userLanguage = getUserLanguage(chatId);
            String button1, button2, button3, button4, button5;

            if (userLanguage.equals("RU")) {
                returnText = "Нажмите одну из следующих кнопок, чтобы выполнить следующее действие " + sighDown;
                button1 = "Сотрудники";
                button2 = "Должности";
                button3 = "Департаменты";
                button4 = "Отделы";
                button5 = "Админы";
            } else {
                returnText = "Keyingi amalni bajarish uchun quyidagi tugmalardan birini bosing " + sighDown;
                button1 = "Xodimlar";
                button2 = "Lavozimlar";
                button3 = "Departamentlar";
                button4 = "Boshqarmalar";
                button5 = "Adminlar";
            }

            ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
            List<KeyboardRow> keyboardRowList = new ArrayList<>();
            replyKeyboardMarkup.setSelective(true);
            replyKeyboardMarkup.setResizeKeyboard(true);
            replyKeyboardMarkup.setOneTimeKeyboard(true);
            keyboardRowList.add(
                    new KeyboardRow(List.of(
                            KeyboardButton.builder()
                                    .text(button1)
                                    .build(),
                            KeyboardButton.builder()
                                    .text(button2)
                                    .build()
                    ))
            );
            keyboardRowList.add(
                    new KeyboardRow(List.of(
                            KeyboardButton.builder()
                                    .text(button3)
                                    .build(),
                            KeyboardButton.builder()
                                    .text(button4)
                                    .build()
                    ))
            );
            keyboardRowList.add(
                    new KeyboardRow(Collections.singleton(
                            KeyboardButton.builder()
                                    .text(button5)
                                    .build()
                    ))
            );
            replyKeyboardMarkup.setKeyboard(keyboardRowList);

            return SendMessage.builder()
                    .replyMarkup(replyKeyboardMarkup)
                    .chatId(String.valueOf(chatId))
                    .text(returnText)
                    .build();
        });
    }

    /***
     * ADMIN role
     */
    public CompletableFuture<SendMessage> adminSectionAdminRoleButtons(Update update) {
        return CompletableFuture.supplyAsync(() -> {

            final var chatId = update.getMessage().getChatId();
            final var userLanguage = getUserLanguage(chatId);
            String button1, button2;

            if (userLanguage.equals("RU")) {
                returnText = "Нажмите одну из следующих кнопок, чтобы выполнить следующее действие " + sighDown;
                button1 = "Инфо Администратора";
                button2 = "Список Админов ";
                mainMenu = "Главное Меню";
            } else {
                returnText = "Keyingi amalni bajarish uchun quyidagi tugmalardan birini bosing " + sighDown;
                button1 = "Admin ma'lumotlari";
                button2 = "Adminlar ro'yxati";
                mainMenu = "Bosh Menu";
            }

            ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
            List<KeyboardRow> keyboardRowList = new ArrayList<>();
            replyKeyboardMarkup.setSelective(true);
            replyKeyboardMarkup.setResizeKeyboard(true);
            replyKeyboardMarkup.setOneTimeKeyboard(true);
            keyboardRowList.add(
                    new KeyboardRow(List.of(
                            KeyboardButton.builder()
                                    .text(button1)
                                    .build(),
                            KeyboardButton.builder()
                                    .text(button2)
                                    .build()
                    ))
            );
            keyboardRowList.add(
                    new KeyboardRow(List.of(
                            KeyboardButton.builder()
                                    .text(mainMenu)
                                    .build()
                    ))
            );
            replyKeyboardMarkup.setKeyboard(keyboardRowList);

            return SendMessage.builder()
                    .replyMarkup(replyKeyboardMarkup)
                    .chatId(String.valueOf(chatId))
                    .text(returnText)
                    .build();
        });
    }

    /***
     * SUPER_ADMIN role
     */
    public CompletableFuture<SendMessage> adminSectionSuperAdminRoleButtons(Update update) {
        return CompletableFuture.supplyAsync(() -> {

            final var chatId = update.getMessage().getChatId();
            final var userLanguage = getUserLanguage(chatId);
            String button1, button2, button3, button4;

            if (userLanguage.equals("RU")) {
                returnText = "Нажмите одну из следующих кнопок, чтобы выполнить следующее действие " + sighDown;
                button1 = "Добавить Админ";
                button2 = "Список Админов ";
                button3 = "Инфо Администратора";
                button4 = "Удалить Админ";
                mainMenu = "Главное Меню";
            } else {
                returnText = "Keyingi amalni bajarish uchun quyidagi tugmalardan birini bosing " + sighDown;
                button1 = "Admin qo'shish";
                button2 = "Adminlar ro'yxati";
                button3 = "Admin ma'lumotlari";
                button4 = "Admin o'chirish";
                mainMenu = "Bosh Menu";
            }

            ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
            List<KeyboardRow> keyboardRowList = new ArrayList<>();
            replyKeyboardMarkup.setSelective(true);
            replyKeyboardMarkup.setResizeKeyboard(true);
            replyKeyboardMarkup.setOneTimeKeyboard(true);
            keyboardRowList.add(
                    new KeyboardRow(List.of(
                            KeyboardButton.builder()
                                    .text(button1)
                                    .build(),
                            KeyboardButton.builder()
                                    .text(button2)
                                    .build()
                    ))
            );
            keyboardRowList.add(
                    new KeyboardRow(List.of(
                            KeyboardButton.builder()
                                    .text(button3)
                                    .build(),
                            KeyboardButton.builder()
                                    .text(button4)
                                    .build()
                    ))
            );
            keyboardRowList.add(
                    new KeyboardRow(List.of(
                            KeyboardButton.builder()
                                    .text(mainMenu)
                                    .build()
                    ))
            );
            replyKeyboardMarkup.setKeyboard(keyboardRowList);


            return SendMessage.builder()
                    .replyMarkup(replyKeyboardMarkup)
                    .chatId(String.valueOf(chatId))
                    .text(returnText)
                    .build();
        });
    }

    /***
     * ADMIN, SUPER_ADMIN roles
     */
    public CompletableFuture<SendMessage> employeeSectionButtons(Update update) {
        return CompletableFuture.supplyAsync(() -> {

            final var chatId = update.getMessage().getChatId();
            final var userLanguage = getUserLanguage(chatId);
            String button1, button2, button3, button4;

            if (userLanguage.equals("RU")) {
                returnText = "Выберите нужное действие в разделе Сотрудники " + sighDown;
                button1 = "Добавить Сотрудники";
                button2 = "Список Сотрудников";
                button3 = "Редактировать Сотрудник";
                button4 = "Удалить Сотрудрик";
                mainMenu = "Главное Меню";
            } else {
                returnText = "Xodimlar bo'limidagi kerakli amalni tanlang " + sighDown;
                button1 = "Xodim qo'shish";
                button2 = "Xodimlar ro'yhati";
                button3 = "Xodimni tahrirlash";
                button4 = "Xodimni o'chirish";
                mainMenu = "Bosh Menu";
            }
            ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
            List<KeyboardRow> keyboardRowList = new ArrayList<>();

            replyKeyboardMarkup.setSelective(true);
            replyKeyboardMarkup.setResizeKeyboard(true);
            replyKeyboardMarkup.setOneTimeKeyboard(true);
            keyboardRowList.add(
                    new KeyboardRow(List.of(
                            KeyboardButton.builder()
                                    .text(button1)
                                    .build(),
                            KeyboardButton.builder()
                                    .text(button2)
                                    .build()
                    ))
            );
            keyboardRowList.add(
                    new KeyboardRow(List.of(
                            KeyboardButton.builder()
                                    .text(button3)
                                    .build(),
                            KeyboardButton.builder()
                                    .text(button4)
                                    .build()
                    ))
            );
            keyboardRowList.add(
                    new KeyboardRow(List.of(
                            KeyboardButton.builder()
                                    .text(mainMenu)
                                    .build()
                    ))
            );
            replyKeyboardMarkup.setKeyboard(keyboardRowList);

            return SendMessage.builder()
                    .replyMarkup(replyKeyboardMarkup)
                    .chatId(String.valueOf(chatId))
                    .text(returnText)
                    .build();
        });
    }

    /***
     * ADMIN, SUPER_ADMIN roles
     */
    public CompletableFuture<SendMessage> positionSectionButtons(Update update) {
        return CompletableFuture.supplyAsync(() -> {

            final var chatId = update.getMessage().getChatId();
            final var userLanguage = getUserLanguage(chatId);
            String button1, button2, button3, button4;

            if (userLanguage.equals("RU")) {
                returnText = "Выберите нужное действие в разделе Должности " + sighDown;
                button1 = "Добавить Должность";
                button2 = "Список Должностов";
                button3 = "Редактировать Должность";
                button4 = "Удалить Должность";
                mainMenu = "Главное Меню";
            } else {
                returnText = "Lavozimlar bo'limidagi kerakli amalni tanlang " + sighDown;
                button1 = "Lavozim qo'shish";
                button2 = "Lavozimlar ro'yhati";
                button3 = "Lavozimni tahrirlash";
                button4 = "Lavozimni o'chirish";
                mainMenu = "Bosh Menu";
            }

            ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
            List<KeyboardRow> keyboardRowList = new ArrayList<>();

            replyKeyboardMarkup.setSelective(true);
            replyKeyboardMarkup.setResizeKeyboard(true);
            replyKeyboardMarkup.setOneTimeKeyboard(true);
            keyboardRowList.add(
                    new KeyboardRow(List.of(
                            KeyboardButton.builder()
                                    .text(button1)
                                    .build(),
                            KeyboardButton.builder()
                                    .text(button2)
                                    .build()
                    ))
            );
            keyboardRowList.add(
                    new KeyboardRow(List.of(
                            KeyboardButton.builder()
                                    .text(button3)
                                    .build(),
                            KeyboardButton.builder()
                                    .text(button4)
                                    .build()
                    ))
            );
            keyboardRowList.add(
                    new KeyboardRow(List.of(
                            KeyboardButton.builder()
                                    .text(mainMenu)
                                    .build()
                    ))
            );
            replyKeyboardMarkup.setKeyboard(keyboardRowList);

            return SendMessage.builder()
                    .replyMarkup(replyKeyboardMarkup)
                    .chatId(String.valueOf(chatId))
                    .text(returnText)
                    .build();
        });
    }

    /***
     * ADMIN, SUPER_ADMIN roles
     */
    public CompletableFuture<SendMessage> departmentSectionButtons(Update update) {
        return CompletableFuture.supplyAsync(() -> {

            final var chatId = update.getMessage().getChatId();
            final var userLanguage = getUserLanguage(chatId);
            String button1, button2, button3, button4;

            if (userLanguage.equals("RU")) {
                returnText = "Выберите нужное действие в разделе Департаменты " + sighDown;
                button1 = "Добавить Департамент";
                button2 = "Список Департаменты";
                button3 = "Редактировать Департамент";
                button4 = "Удалить Департамент";
                mainMenu = "Главное Меню";
            } else {
                returnText = "Departamentlar bo'limidagi kerakli amalni tanlang " + sighDown;
                button1 = "Departament qo'shish";
                button2 = "Departamentlar ro'yhati";
                button3 = "Departamentni tahrirlash";
                button4 = "Departamentni o'chirish";
                mainMenu = "Bosh Menu";
            }

            ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
            List<KeyboardRow> keyboardRowList = new ArrayList<>();

            replyKeyboardMarkup.setSelective(true);
            replyKeyboardMarkup.setResizeKeyboard(true);
            replyKeyboardMarkup.setOneTimeKeyboard(true);
            keyboardRowList.add(
                    new KeyboardRow(List.of(
                            KeyboardButton.builder()
                                    .text(button1)
                                    .build(),
                            KeyboardButton.builder()
                                    .text(button2)
                                    .build()
                    ))
            );
            keyboardRowList.add(
                    new KeyboardRow(List.of(
                            KeyboardButton.builder()
                                    .text(button3)
                                    .build(),
                            KeyboardButton.builder()
                                    .text(button4)
                                    .build()
                    ))
            );
            keyboardRowList.add(
                    new KeyboardRow(List.of(
                            KeyboardButton.builder()
                                    .text(mainMenu)
                                    .build()
                    ))
            );
            replyKeyboardMarkup.setKeyboard(keyboardRowList);

            return SendMessage.builder()
                    .replyMarkup(replyKeyboardMarkup)
                    .chatId(String.valueOf(chatId))
                    .text(returnText)
                    .build();
        });
    }

    /***
     * ADMIN, SUPER_ADMIN roles
     */
    public CompletableFuture<SendMessage> managementSectionButtons(Update update) {
        return CompletableFuture.supplyAsync(() -> {

            final var chatId = update.getMessage().getChatId();
            final var userLanguage = getUserLanguage(chatId);
            String button1, button2, button3, button4;

            if (userLanguage.equals("RU")) {
                returnText = "Выберите нужное действие в разделе Отделы " + sighDown;
                button1 = "Добавить Отдел";
                button2 = "Список Отделы";
                button3 = "Редактировать Отдел";
                button4 = "Удалить Отдел";
                mainMenu = "Главное Меню";
            } else {
                returnText = "Boshqarmalar bo'limidagi kerakli amalni tanlang " + sighDown;
                button1 = "Boshqarma qo'shish";
                button2 = "Boshqarmalar ro'yhati";
                button3 = "Boshqarmalarni tahrirlash";
                button4 = "Boshqarmani o'chirish";
                mainMenu = "Bosh Menu";
            }

            ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
            List<KeyboardRow> keyboardRowList = new ArrayList<>();

            replyKeyboardMarkup.setSelective(true);
            replyKeyboardMarkup.setResizeKeyboard(true);
            replyKeyboardMarkup.setOneTimeKeyboard(true);
            keyboardRowList.add(
                    new KeyboardRow(List.of(
                            KeyboardButton.builder()
                                    .text(button1)
                                    .build(),
                            KeyboardButton.builder()
                                    .text(button2)
                                    .build()
                    ))
            );
            keyboardRowList.add(
                    new KeyboardRow(List.of(
                            KeyboardButton.builder()
                                    .text(button3)
                                    .build(),
                            KeyboardButton.builder()
                                    .text(button4)
                                    .build()
                    ))
            );
            keyboardRowList.add(
                    new KeyboardRow(List.of(
                            KeyboardButton.builder()
                                    .text(mainMenu)
                                    .build()
                    ))
            );
            replyKeyboardMarkup.setKeyboard(keyboardRowList);

            return SendMessage.builder()
                    .replyMarkup(replyKeyboardMarkup)
                    .chatId(String.valueOf(chatId))
                    .text(returnText)
                    .build();
        });
    }

    private String getUserLanguage(Long userChatId) {
        return userRepository.getUserLanguageByUserChatId(userChatId);
    }

    private List<String> getDepartmentNames() {
        return departmentRepository.getDepartmentNames();
    }

    private List<Employee> getDepartmentEmployees(Long departmentId) {
        return employeeRepository.getEmployeesByPosition_Management_Department_Id(departmentId);
    }

    private List<Employee> getManagementEmployees(Long departmentId) {
        return employeeRepository.getEmployeesByPosition_Management_Id(departmentId);
    }

    private List<Employee> getPositionEmployees(Long position_id){
        return employeeRepository.getEmployeesByPosition_Id(position_id);
    }

    private List<String> getDepartmentEmployeesNames(Long department_id) {
        List<String> employeeNames = new ArrayList<>();
        for (Employee departmentEmployee : getDepartmentEmployees(department_id)) {
            employeeNames.add(departmentEmployee.getFullName());
        }
        return employeeNames;
    }

    private List<String> getManagementNames() {
        return managementRepository.getManagementNames();
    }

    private List<String> getPositionNames() {
        return positionRepository.getPositionNames();
    }

    public List<Employee> getEmployees() {
        return employeeRepository.findAll();
    }

}