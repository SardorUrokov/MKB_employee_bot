package com.example.mkb_employee_bot;

import org.springframework.boot.SpringApplication;
import com.example.mkb_employee_bot.component.bot.EmployeeBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@SpringBootApplication
public class MkbEmployeeBotApplication {
    public static void main(String[] args) throws TelegramApiException {
        SpringApplication.run(MkbEmployeeBotApplication.class, args);

        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        try {
            telegramBotsApi.registerBot(new EmployeeBot());
        } catch (Exception e){e.printStackTrace();}
    }
}