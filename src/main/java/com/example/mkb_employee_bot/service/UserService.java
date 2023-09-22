package com.example.mkb_employee_bot.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.mkb_employee_bot.entity.User;
import com.example.mkb_employee_bot.entity.enums.Role;
import com.example.mkb_employee_bot.entity.enums.Language;
import com.example.mkb_employee_bot.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public void register(User user) {

        final var userRole = user.getRole();
        User saving = User.builder()
                .userChatId(user.getUserChatId())
                .phoneNumber(user.getPhoneNumber())
                .fullName(user.getFullName())
                .userName(user.getUserName())
                .createdAt(new Date())
                .updatedAt(new Date())
                .role(userRole == null ? Role.USER : userRole)
                .language(Language.UZ)
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

    public List<Long> getUsersChatIdsBySameDepartmentEmployeesPhoneNumber(List<String> phoneNumbers) {

        List<Long> usersChatIds = new ArrayList<>();
        final var userList = userRepository.findByPhoneNumberIn(phoneNumbers);

        if (!userList.isEmpty()) {
            for (User user : userList) {
                usersChatIds.add(user.getUserChatId());
            }
        }
        return usersChatIds;
    }
}