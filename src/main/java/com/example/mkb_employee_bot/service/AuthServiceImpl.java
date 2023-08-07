package com.example.mkb_employee_bot.service;

import java.util.Date;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.mkb_employee_bot.entiry.User;
import com.example.mkb_employee_bot.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl {

    private final UserRepository userRepository;

    public void register(User user) {

        User saving = User.builder()
                .userChatId(user.getUserChatId())
                .fullName(user.getFullName())
                .userName(user.getUserName())
                .createdAt(new Date())
                .role(user.getRole())
                .language(user.getLanguage())
                .build();

        final User saved = userRepository.save(saving);
        log.info("User Created -> {}", saved);
    }
}