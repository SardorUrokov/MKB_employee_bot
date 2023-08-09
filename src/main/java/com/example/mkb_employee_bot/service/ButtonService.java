package com.example.mkb_employee_bot.service;

import com.example.mkb_employee_bot.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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
            String button1 = "", button2 = "", button3 = "", button4 = "", button5 = "";

            if (userLanguage.equals("RU")) {
                returnText = "Выберите нужный раздел для получения информации " + sighDown;
                button1 = "Сотрудники";
                button2 = "Должности";
                button3 = "Департаменты";
                button4 = "Отделы";
//                button5 = "Админы";
            } else {
                returnText = "Ma'lumot olish uchun kerakli bo'limni tanlang " + sighDown;
                button1 = "Xodimlar";
                button2 = "Lavozimlar";
                button3 = "Departamentlar";
                button4 = "Boshqarmalar";
//                button5 = "Adminlar";
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
    public CompletableFuture<SendMessage> departmentSectionUserRoleButtons(Update update) {
        return CompletableFuture.supplyAsync(() -> {

            final var chatId = update.getMessage().getChatId();
            final var userLanguage = getUserLanguage(chatId);
            final var departmentNames = getDepartmentNames();
            String button1 = "", button2 = "";

            if (userLanguage.equals("RU")) {
                returnText = "Выберите нужный Департамент из списка " + sighDown;
                mainMenu = "Главное Меню";
            } else {
                returnText = "Ro'yxatdan kerakli Departamentni tanlang " + sighDown;
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
    public CompletableFuture<SendMessage> superAdminButtons(Update update) {
        return CompletableFuture.supplyAsync(() -> {

            final var chatId = update.getMessage().getChatId();
            final var userLanguage = getUserLanguage(chatId);
            String button1 = "", button2 = "", button3 = "", button4 = "", button5 = "";

            if (userLanguage.equals("RU")) {
                returnText = "Нажмите одну из следующих кнопок, чтобы выполнить следующее действие " + sighDown;
                button1 = "Сотрудники";
                button2 = "Должности";
                button3 = "Департаменты";
                button4 = "Отделы";
                button5 = "Админы";
//                mainMenu = "Главное Меню";
//                back = "Назад " + sighBack;
            } else {
                returnText = "Keyingi amalni bajarish uchun quyidagi tugmalardan birini bosing " + sighDown;
                button1 = "Xodimlar";
                button2 = "Lavozimlar";
                button3 = "Departamentlar";
                button4 = "Boshqarmalar";
                button5 = "Adminlar";
//                mainMenu = "Bosh Menu";
//                back = "Orqaga " + sighBack;
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
            String button1 = "", button2 = "";

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
            String button1 = "", button2 = "", button3 = "", button4 = "";

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
            String button1 = "", button2 = "", button3 = "", button4 = "";

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
            String button1 = "", button2 = "", button3 = "", button4 = "";

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
            String button1 = "", button2 = "", button3 = "", button4 = "";

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

    private List<String> getDepartmentNames(){
        return departmentRepository.getDepartmentNames();
    }
}