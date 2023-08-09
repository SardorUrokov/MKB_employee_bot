package com.example.mkb_employee_bot.component;

import java.util.Date;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.boot.CommandLineRunner;
import com.example.mkb_employee_bot.entiry.User;
import com.example.mkb_employee_bot.entiry.enums.Role;
import com.example.mkb_employee_bot.entiry.enums.Language;
import com.example.mkb_employee_bot.repository.UserRepository;

@Component
@RequiredArgsConstructor
public class Dataloader implements CommandLineRunner {

    private final UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {

        final var userList = userRepository.findAll();
        if (userList.isEmpty()) {

            User user = User.builder()
                    .userChatId(632993372L)
                    .fullName("Сардор")
                    .phoneNumber("998914525468")
                    .userName("Sardor_Shukhratovich")
                    .role(Role.SUPER_ADMIN)
                    .language(Language.RU)
                    .createdAt(new Date())
                    .updatedAt(new Date())
                    .build();
            userRepository.save(user);
        }
    }
}