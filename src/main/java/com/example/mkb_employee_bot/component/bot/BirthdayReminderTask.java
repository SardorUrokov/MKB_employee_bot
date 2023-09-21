package com.example.mkb_employee_bot.component.bot;

import com.example.mkb_employee_bot.entity.Employee;
import com.example.mkb_employee_bot.repository.BinaryContentRepository;
import com.example.mkb_employee_bot.repository.UserRepository;
import com.example.mkb_employee_bot.service.BotService;
import com.example.mkb_employee_bot.service.EmployeeServiceImpl;
import com.example.mkb_employee_bot.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BirthdayReminderTask {

    private final UserService userService;
    private final BotService telegramBotService;
    private final EmployeeServiceImpl employeeService;

    @Scheduled(cron = "0 0 8 * * ?", zone = "Asia/Tashkent") //daily at 8AM (08:00)
    public void remindBirthdays() {

        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        String today = currentDate.format(formatter);
        List<Employee> employeesWithBirthday = employeeService.getEmployeesWithBirthday(today);

        if (!employeesWithBirthday.isEmpty()) {
            for (Employee babyEmployee : employeesWithBirthday) {

                final var departmentId = babyEmployee.getPosition().getManagement().getDepartment().getId();

                List<String> colleaguesPhoneNumbers = employeeService.getDepartmentEmployeesPhoneNumbers(departmentId);
                List<Long> colleaguesChatIds = userService.getUsersChatIdsBySameDepartmentEmployeesPhoneNumber(colleaguesPhoneNumbers);

                EmployeeBot employeeBot = new EmployeeBot();
                employeeBot.sendBirthdayMessages(babyEmployee, colleaguesChatIds);
            }
        }
    }
}
