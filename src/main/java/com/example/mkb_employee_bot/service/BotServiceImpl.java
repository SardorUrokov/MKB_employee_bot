package com.example.mkb_employee_bot.service;

import com.example.mkb_employee_bot.entiry.*;
import com.example.mkb_employee_bot.entiry.enums.EduType;
import com.example.mkb_employee_bot.entiry.enums.SkillType;
import com.example.mkb_employee_bot.entiry.enums.Stage;
import com.example.mkb_employee_bot.repository.*;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendGame;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static com.example.mkb_employee_bot.entiry.enums.SkillType.HARD_SKILL;
import static com.example.mkb_employee_bot.entiry.enums.SkillType.SOFT_SKILL;

@Slf4j
@Service
@RequiredArgsConstructor
public class BotServiceImpl {

    private final AuthServiceImpl authService;
    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final PositionRepository positionRepository;
    private final EmployeeRepository employeeRepository;
    private final EducationRepository educationRepository;
    private final DepartmentRepository departmentRepository;
    private final ManagementRepository managementRepository;
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
                    userRepository.updateUserStageByUserChatId(chatId, Stage.LANGUAGE_SELECTED.name());

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

    public String getUserLanguage(Long userChatId) {
        return userRepository.getUserLanguageByUserChatId(userChatId);
    }

    public String getUserRole(Long userChatId) {
        return userRepository.getUserRoleByUserChatId(userChatId);
    }

    public CompletableFuture<SendMessage> getSelectedEmployeeInfo(Update update) {
        return CompletableFuture.supplyAsync(() -> {

            final var chatId = update.getMessage().getChatId();
            final var employee = employeeRepository.findByFullName(update.getMessage().getText()).orElseThrow(NotFoundException::new);
            final var info = getEmployeeInfoForUserLanguage_UZ(employee);
            String buttonText = getUserLanguage(chatId).equals("UZ") ? "Bosh Menu" : "Главное Меню";

            List<KeyboardRow> keyboardRowList = new ArrayList<>();
            ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
            replyKeyboardMarkup.setSelective(true);
            replyKeyboardMarkup.setResizeKeyboard(true);
            replyKeyboardMarkup.setOneTimeKeyboard(true);
            keyboardRowList.add(
                    new KeyboardRow(
                            Collections.singleton(
                                    KeyboardButton.builder()
                                            .text(buttonText)
                                            .build()
                            )
                    )
            );
            replyKeyboardMarkup.setKeyboard(keyboardRowList);

            return SendMessage.builder()
                    .replyMarkup(replyKeyboardMarkup)
                    .chatId(String.valueOf(chatId))
                    .text(info)
                    .build();

        });
    }

    public String getMessageSection(String messageText) {

        String section = "";
        for (Position position : positionRepository.findAll()) {
            if (position.getName().equals(messageText)) {
                section = "positionSection";
                break;
            }
        }
        for (Department department : departmentRepository.findAll()) {
            if (department.getName().equals(messageText)) {
                section = "departmentSection";
                break;
            }
        }
        for (Management management : managementRepository.findAll()) {
            if (management.getName().equals(messageText)) {
                section = "managementSection";
                break;
            }
        }

        return section;
    }

    private String getEmployeeInfoForUserLanguage_UZ(Employee employee) {
        return "Xodim" +
                "\nIsm Familiyasi: " + employee.getFullName() +
                "\nTug'ilgan sanasi: " + employee.getDateOfBirth() +
                "\nYoshi: " + employee.getAge() +
                "\nMillati: " + employee.getNationality() +
                "\nLavozim: " + employee.getPosition().getName() +
                "\nBo'lim: " + employee.getPosition().getManagement().getName() +
                "\nDepartament: " + employee.getPosition().getManagement().getDepartment().getName() + "\n" +
                "\nMa'lumoti " + getEmployeeEducationsInfo(employee) +
                "\nMalakasi\n" + getEmployeeSkills(employee);
    }

    private String getEmployeeSkills(Employee employee) {

        String hardSkills = "", softSkills = "";
        StringBuilder stringBuilder = new StringBuilder();
        StringBuilder stringBuilder1 = new StringBuilder();
        final var employeeSkillsIds = employeeRepository.getEmployeeSkillsIds(employee.getId());

        for (Skill skill : skillRepository.findSkillsByIdsAndSkillType(employeeSkillsIds, HARD_SKILL)) {
            hardSkills = String.valueOf(stringBuilder.append(skill.getName()).append(", "));
        }

        for (Skill skill : skillRepository.findSkillsByIdsAndSkillType(employeeSkillsIds, SOFT_SKILL)) {
            softSkills = String.valueOf(stringBuilder1.append(skill.getName()).append(", "));
        }

        return checkCommas(hardSkills) + "\n" + checkCommas(softSkills) + " ;";
    }

    private String checkCommas(String string) {

        String substringed = "";
        if (string.endsWith(", ")) {
            final var length = string.length();
            substringed = string.substring(0, length - 2);
        }
        return substringed;
    }

    private String getEmployeeEducationsInfo(Employee employee) {

        String educationInfo = "";
        final var educations = employee.getEducations();
        final var educationsIds = employeeRepository.getEmployeeEducationsIds(employee.getId());

        for (Education item : educations) {

            int value, preValue = 0;
            final var type = item.getType();
            StringBuilder stringBuilder = new StringBuilder();

            for (Education education : educationRepository.findEducationByIdIn(educationsIds)) {

                value = education.getType().getValue();
                if (!(value < preValue)) {
                    educationInfo = String.valueOf(stringBuilder.append(setEduInfos(education)).append(" "));
                }
                preValue = value;
            }
        }

        return educationInfo;
    }

    private String setEduInfos(Education education) {
        return "\nTa'lim muassasi: " + education.getName() +
                "\nTa'lim yo'nalishi: " + education.getEducationField() +
                "\n" + education.getType() +
                "\nMuddatlari: (" + education.getStartedDate() + " - " + education.getEndDate() + ")\n";
    }
}