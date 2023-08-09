package com.example.mkb_employee_bot.service;

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

                    List<KeyboardRow> keyboardRowList = new ArrayList<>();
                    ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
                    replyKeyboardMarkup.setSelective(true);
                    replyKeyboardMarkup.setResizeKeyboard(true);
                    replyKeyboardMarkup.setOneTimeKeyboard(true);
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

    private String getUserLanguage(Long userChatId) {
        return userRepository.getUserLanguageByUserChatId(userChatId);
    }

    public String getUserRole(Long userChatId) {
        return userRepository.getUserRoleByUserChatId(userChatId);
    }
}