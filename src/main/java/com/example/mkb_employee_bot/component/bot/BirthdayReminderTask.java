package com.example.mkb_employee_bot.component.bot;

import java.util.List;
import java.time.LocalDate;

import com.example.mkb_employee_bot.entity.Employee;
import com.example.mkb_employee_bot.service.UserService;
import com.example.mkb_employee_bot.repository.UserRepository;
import com.example.mkb_employee_bot.service.EmployeeServiceImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;

@Component
@RequiredArgsConstructor
public class BirthdayReminderTask {

    private final UserRepository userRepository;
    private final UserService userService;
    private final EmployeeServiceImpl employeeService;

    @Scheduled(cron = "0 30 9 * * ?", zone = "Asia/Tashkent") //daily at 09:30 AM
    public void remindBirthdays() {

        List<Employee> employeesWithBirthday = employeeService.getEmployeesWithBirthday();

        if (!(employeesWithBirthday.isEmpty())) {
            for (Employee babyEmployee : employeesWithBirthday) {

                final var departmentId = babyEmployee.getPosition().getManagement().getDepartment().getId();

                List<String> colleaguesPhoneNumbers = employeeService.getDepartmentEmployeesPhoneNumbers(departmentId);
                List<Long> colleaguesChatIds = userService.getUsersChatIdsBySameDepartmentEmployeesPhoneNumber(colleaguesPhoneNumbers);
                final var babyEmployeeUser = userRepository.findByPhoneNumber(babyEmployee.getPhoneNumber());

                EmployeeBot employeeBot = new EmployeeBot();
                if (!colleaguesChatIds.isEmpty())
                    employeeBot.sendBirthdayMessageToColleagues(babyEmployee, colleaguesChatIds); // send message to babyEmployee's Colleagues about today is BabyEmployee's birthday

                babyEmployeeUser.ifPresent(
                        user -> employeeBot.sendCongratulation(babyEmployee, user.getUserChatId()) //send Birthday message to BabyEmployee
                );

                employeeService.increaseEmployeeAge(babyEmployee);
            }
        } else {
            LocalDate today = LocalDate.now();
            final var month = today.getMonth();
            final var dayOfMonth = today.getDayOfMonth();
            System.out.println("No employees born on " + dayOfMonth + " " + month.toString().toUpperCase() + " were found!");
        }
    }
}