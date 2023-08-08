package com.example.mkb_employee_bot.service;

import com.example.mkb_employee_bot.entiry.enums.Language;
import lombok.RequiredArgsConstructor;
import com.example.mkb_employee_bot.entiry.User;
import com.example.mkb_employee_bot.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class BotServiceImpl {

    private final UserRepository userRepository;
    private final AuthServiceImpl authService;
    private String returnText = "";

    public void registerUser(Update update) {

        CompletableFuture.runAsync(() -> {
            Long chatId = update.getMessage().getChatId();
            final var user = update.getMessage().getFrom();
//            final var phoneNumber = update.getMessage().getContact().getPhoneNumber();
            final var userName = user.getUserName();
            final var firstName = user.getFirstName();
            final var lastName = user.getLastName() == null ? ("") : user.getLastName();
            final var fullName = firstName + " " + lastName;

            Optional<User> optionalUser = userRepository.findByUserChatId(chatId);

            if (optionalUser.isEmpty()) {
                authService.register(
                        User.builder()
                                .userChatId(chatId)
                                .fullName(fullName)
                                .userName(userName)
                                .build()
                );
            }
        });
    }

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

    public CompletableFuture<SendMessage> setUserLanguage(Update update) {
        return CompletableFuture.supplyAsync(() -> {
                    registerUser(update);

                    final var updateMessage = update.getMessage();
                    final var chatId = update.getMessage().getChatId();
                    String buttonText = "";
                    String language = "";

                    if ("\uD83C\uDDFA\uD83C\uDDFF".equals(updateMessage.getText())) {
                        returnText = "Iltimos telefon raqamingizni jo'nating";
                        buttonText = "Kontaktni ulashish";
                        language = "UZ";
                    } else {
                        returnText = "Пожалуйста, отправьте ваш номер телефона";
                        buttonText = "Поделиться Контакт";
                        language = "RU";
                    }
                    userRepository.updateLanguageByUserId(chatId, language);

                    ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
                    replyKeyboardMarkup.setSelective(true);
                    replyKeyboardMarkup.setResizeKeyboard(true);
                    replyKeyboardMarkup.setOneTimeKeyboard(true);
                    List<KeyboardRow> keyboardRowList = new ArrayList<>();

                    keyboardRowList.add(
                            new KeyboardRow(Collections.singleton(
                                    KeyboardButton.builder()
                                            .requestContact(true)
                                            .text(buttonText)
                                            .build()
                            ))
                    );
                    replyKeyboardMarkup.setKeyboard(keyboardRowList);

                    return SendMessage.builder()
                            .replyMarkup(replyKeyboardMarkup)
                            .chatId(String.valueOf(chatId))
                            .text(returnText)
                            .build();
                }
        );
    }

    public void setPhoneNumber(Update update) {

        CompletableFuture.runAsync(() -> {
                    String phoneNumber = update.getMessage().getContact().getPhoneNumber();
                    final var chatId = update.getMessage().getChatId();
                    final var exists = userRepository.existsByUserChatId(chatId);

                    if (exists) {
                        final var user = userRepository.findByUserChatId(chatId).orElseThrow();

                        if (user.getPhoneNumber() == null) {
                            userRepository.updatePhoneNumberByUserId(chatId, phoneNumber);
                            log.info("User's Contact is updated -> User ID: {} PhoneNumber: {} ", chatId, phoneNumber);
                        }
                    }
                }
        );
    }

    public String getUserLanguage(Long userChatId) {
        return userRepository.getUserLanguageByUserChatId(userChatId);
    }

    public String getUserRole(Long userChatId) {
        return userRepository.getUserRoleByUserChatId(userChatId);
    }
}