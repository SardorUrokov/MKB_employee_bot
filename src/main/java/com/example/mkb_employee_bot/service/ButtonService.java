package com.example.mkb_employee_bot.service;

import com.example.mkb_employee_bot.repository.UserRepository;
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

    private final AuthServiceImpl authService;
    private final UserRepository userRepository;

    private String returnText = "";
    private String mainMenu = "";
    private String back = "";
    private final String sighBack = "⬅\uFE0F";
    private final String sighDown = "⬇\uFE0F";


    public CompletableFuture<SendMessage> selectLanguageButtons(Update update) {

        return CompletableFuture.supplyAsync(() -> {

                    ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
                    replyKeyboardMarkup.setSelective(true);
                    replyKeyboardMarkup.setOneTimeKeyboard(true);
                    replyKeyboardMarkup.setResizeKeyboard(true);
                    List<KeyboardRow> keyboardRowList = new ArrayList<>();

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

    public CompletableFuture<SendMessage> adminSectionAdminRoleButtons (Update update){
        return CompletableFuture.supplyAsync(() -> {

            final var chatId = update.getMessage().getChatId();
            final var userLanguage = getUserLanguage(chatId);
            String button1 = "", button2 = "", button3 = "";

            if (userLanguage.equals("RU")) {
                returnText = "Нажмите одну из следующих кнопок, чтобы выполнить следующее действие " + sighDown;
                button1 = "Инфо Администратора";
                button2 = "Список Админов ";
                button3 = "Админы";
//                mainMenu = "Главное Меню";
//                back = "Назад " + sighBack;
            } else {
                returnText = "Keyingi amalni bajarish uchun quyidagi tugmalardan birini bosing " + sighDown;
                button1 = "Admin info";
                button2 = "Adminlar ro'yxati";
                button3 = "Adminlar";
//                mainMenu = "Bosh Menu";
//                back = "Orqaga " + sighBack;
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
}