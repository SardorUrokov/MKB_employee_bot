//package com.example.mkb_employee_bot.component.bot;
//
//import com.example.mkb_employee_bot.entity.Employee;
//import com.example.mkb_employee_bot.service.BotService;
//import com.example.mkb_employee_bot.service.EmployeeServiceImpl;
//import lombok.RequiredArgsConstructor;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import java.time.LocalDate;
//import java.time.format.DateTimeFormatter;
//import java.util.List;
//
//@Component
//@RequiredArgsConstructor
//public class BirthdayReminderTask {
//
//    private final EmployeeServiceImpl employeeService;
//    private final BotService telegramBotService;
//
//    @Scheduled(cron = "0 0 8 * * ?", zone = "Asia/Tashkent") //daily at 8AM
//    public void remindBirthdays() {
//
//        LocalDate currentDate = LocalDate.now();
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//
//        String today = currentDate.format(formatter);
//        List<Employee> employeesWithBirthday = employeeService.getEmployeesWithBirthday(today);
//
//        for (Employee employee : employeesWithBirthday) {
//
//            List<Employee> colleagues = employeeService.getColleaguesInSameDepartment(employee);
////            List<String> phoneNumbers = employeeService.getUsersPhoneNumbersWhoseSameDepartmentEmployees();
////            telegramBotService.sendBirthdayMessages(employee, colleagues, phoneNumbers);
//        }
//    }
//}
