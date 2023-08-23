package com.example.mkb_employee_bot.service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.example.mkb_employee_bot.entity.enums.Role;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.mkb_employee_bot.entity.User;
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
                .role(Role.USER)
                .language(user.getLanguage())
                .build();

        final User saved = userRepository.save(saving);
        log.info("User Created -> {}", saved);
    }

    public boolean deleteUser(String phoneNumber) {
        final var user = userRepository.findByPhoneNumber(phoneNumber).orElseThrow();

        if (user.getRole().name().equals("SUPER_ADMIN")) {
            final var superAdminUsers = userRepository.findAllByRole(Role.SUPER_ADMIN);

            if (superAdminUsers.size() == 1 || superAdminUsers.isEmpty())
                return false;
            else {
                userRepository.updateRoleToUSERByPhoneNumber("USER", phoneNumber);
                return true;
            }
        } else {
            userRepository.updateRoleToUSERByPhoneNumber("USER", phoneNumber);
            return true;
        }
    }
}