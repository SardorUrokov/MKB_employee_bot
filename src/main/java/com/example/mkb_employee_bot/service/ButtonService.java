package com.example.mkb_employee_bot.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.example.mkb_employee_bot.entity.*;
import com.example.mkb_employee_bot.entity.enums.EduType;
import com.example.mkb_employee_bot.entity.enums.FileType;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import com.example.mkb_employee_bot.repository.*;
import com.example.mkb_employee_bot.entity.enums.Stage;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;

import static com.example.mkb_employee_bot.entity.enums.SkillType.HARD_SKILL;
import static com.example.mkb_employee_bot.entity.enums.SkillType.SOFT_SKILL;

@Service
@RequiredArgsConstructor
public class ButtonService {

    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final EmployeeRepository employeeRepository;
    private final PositionRepository positionRepository;
    private final EducationRepository educationRepository;
    private final DepartmentRepository departmentRepository;
    private final ManagementRepository managementRepository;

    private final DepartmentServiceImpl departmentService;

    private final String back = "";
    private String mainMenu = "";
    private final String bosh_Menu = "Bosh Menu ↩️";
    private final String главное_Меню = "Главное Меню ↩️";
    private String returnText = "";
    private final String sighDown = "⬇️";
    private final String sighBack = "⬅️";
    private Long chatId;
    private String userLanguage = "";

    private int userStageIndex = 0; // Initialize with the starting stage index
    List<String> steps_uz = new ArrayList<>();
    List<String> steps_ru = new ArrayList<>();
    public boolean moveToNext = true;

    private String getSteps_uz(int index) {

        steps_uz.add("Xodimning ism-familiyasini kiriting");
        steps_uz.add("""
                Xodimning tug'ilgan sanasi:

                ❗️Namuna: 1999-12-31 (yyyy-mm-dd)""");

        steps_uz.add("Millati:");
        steps_uz.add("Ta'lim bosqichini tanlang " + sighDown);
        steps_uz.add("Ta'lim muassasa nomi:");
        steps_uz.add("Ta'lim yo'nalishi nomi:");

        steps_uz.add("""
                Muddatlari:
                        
                ❗️Namuna: 2018-2022;
                ❗️Agar hozirda davom etayotgan bo'lsa: 2020-Present""");

        steps_uz.add("""
                Xodimning malakasini kiriting:
                        
                ❗️Namuna: PostgreSQl, JAVA, Problem Solving, Managerial Ability...""");

        steps_uz.add("Kiritish uchun rasm yoki resumeni tanlang ⬇️");
        return steps_uz.get(index);
    }

    private String getSteps_ru(int index) {

        steps_ru.add("Введите имя и фамилию сотрудника");
        steps_ru.add("""
                Дата рождения сотрудника:

                ❗️Образец: 1999-12-31 (гггг-мм-дд)""");

        steps_ru.add("Национальность:");
        steps_ru.add("Выберите уровень образования:" + sighDown);
        steps_ru.add("Название учебного заведения:");
        steps_ru.add("Название направления обучения:");

        steps_ru.add("""
                Сроки выполнения:

                ❗️Образец: 2018-2022;
                ❗️Если в данный момент продолжается: 2020-Present""");

        steps_ru.add("""
                Введите навык сотрудника:
                        
                ❗️Образец: PostgreSQl, JAVA, Problem Solving, Управленческие способности...""");

        steps_ru.add("Выберите изображение или резюме для вложения ⬇️");
        return steps_ru.get(index);
    }

    public CompletableFuture<SendMessage> selectLanguageButtons(Update update) {

        return CompletableFuture.supplyAsync(() -> {
                    ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
                    List<KeyboardRow> keyboardRowList = new ArrayList<>();
                    replyKeyboardMarkup.setSelective(true);
                    replyKeyboardMarkup.setOneTimeKeyboard(true);
                    replyKeyboardMarkup.setResizeKeyboard(true);
                    keyboardRowList.add(
                            new KeyboardRow(List.of(
                                    KeyboardButton.builder()
                                            .text("\uD83C\uDDFA\uD83C\uDDFF")
                                            .build(),
                                    KeyboardButton.builder()
                                            .text("\uD83C\uDDF7\uD83C\uDDFA")
                                            .build()
                            ))
                    );
                    returnText = """
                            Iltimos, botdan foydalanish uchun tilni tanlang\uD83C\uDDFA\uD83C\uDDFF\s

                            Пожалуйста, выберите язык для использования бота\uD83C\uDDF7\uD83C\uDDFA\s""";

                    chatId = update.getMessage().getChatId();
                    replyKeyboardMarkup.setKeyboard(keyboardRowList);

                    return SendMessage.builder()
                            .replyMarkup(replyKeyboardMarkup)
                            .chatId(String.valueOf(chatId))
                            .text(returnText)
                            .build();
                }
        );
    }

    /***
     * USER role
     */
    public CompletableFuture<SendMessage> userButtons(Update update) {
        return CompletableFuture.supplyAsync(() -> {

                    chatId = update.getMessage().getChatId();
                    userLanguage = getUserLanguage(chatId);
                    String button1, button2, button3, button4, languageButton;

                    if (userLanguage.equals("RU")) {
                        returnText = "Выберите нужный раздел для получения информации " + sighDown;
                        button1 = "Сотрудник";
                        button2 = "Должности";
                        button3 = "Департаменты";
                        button4 = "Отделы";
                        languageButton = "Изменить язык \uD83C\uDDF7\uD83C\uDDFA / \uD83C\uDDFA\uD83C\uDDFF";
                    } else {
                        returnText = "Ma'lumot olish uchun kerakli bo'limni tanlang " + sighDown;
                        button1 = "Xodim";
                        button2 = "Lavozimlar";
                        button3 = "Departamentlar";
                        button4 = "Boshqarmalar";
                        languageButton = "Tilni o'zgartirish \uD83C\uDDFA\uD83C\uDDFF / \uD83C\uDDF7\uD83C\uDDFA";
                    }

                    ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
                    replyKeyboardMarkup.setSelective(true);
                    replyKeyboardMarkup.setResizeKeyboard(true);
                    replyKeyboardMarkup.setOneTimeKeyboard(true);
                    List<KeyboardRow> keyboardRowList = new ArrayList<>();

                    keyboardRowList.add(
                            new KeyboardRow(List.of(
                                    KeyboardButton.builder()
                                            .text(button1)
                                            .build(),
                                    KeyboardButton.builder()
                                            .text(button2)
                                            .build()
                            ))
                    );
                    keyboardRowList.add(
                            new KeyboardRow(List.of(
                                    KeyboardButton.builder()
                                            .text(button3)
                                            .build(),
                                    KeyboardButton.builder()
                                            .text(button4)
                                            .build()
                            ))
                    );
                    keyboardRowList.add(
                            new KeyboardRow(
                                    Collections.singletonList(
                                            KeyboardButton.builder()
                                                    .text(languageButton)
                                                    .build()
                                    )
                            )
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

    /***
     * USER role
     */
    public CompletableFuture<SendMessage> positionEmployees(Update update) {
        return CompletableFuture.supplyAsync(() -> {

                    chatId = update.getMessage().getChatId();
                    userLanguage = getUserLanguage(chatId);
                    final var department = positionRepository.findByName(update.getMessage().getText()).orElseThrow(NotFoundException::new);
                    final var managementEmployees = getManagementEmployees(department.getId());

                    if (userLanguage.equals("RU")) {
                        if (managementEmployees.isEmpty())
                            returnText = "Список пустой, сотрудников на данной Должности нет в списке.";
                        else
                            returnText = "Выберите нужного Сотрудника из списка " + sighDown;
                        mainMenu = главное_Меню;
                    } else {
                        if (managementEmployees.isEmpty())
                            returnText = "Ro'yhat bo'sh, ushbu Bo'limdagi xodimlar ro'yxatda yo'q.";
                        else
                            returnText = "Ro'yxatdan kerakli xodimni tanlang " + sighDown;
                        mainMenu = bosh_Menu;
                    }

                    userRepository.updateUserStageByUserChatId(chatId, Stage.SELECTED_EMPLOYEE_NAME_FOR_SEARCH_ROLE_USER.name());
                    ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
                    List<KeyboardRow> keyboardRowList = new ArrayList<>();
                    replyKeyboardMarkup.setSelective(true);
                    replyKeyboardMarkup.setResizeKeyboard(true);
                    replyKeyboardMarkup.setOneTimeKeyboard(true);

                    for (Employee employee : managementEmployees) {
                        keyboardRowList.add(
                                new KeyboardRow(
                                        Collections.singletonList(
                                                KeyboardButton.builder()
                                                        .text(employee.getFullName())
                                                        .build()
                                        )
                                )
                        );
                    }

                    keyboardRowList.add(
                            new KeyboardRow(List.of(
                                    KeyboardButton.builder()
                                            .text(mainMenu)
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

    /***
     * USER role
     */
    public CompletableFuture<SendMessage> managementEmployees(Update update) {
        return CompletableFuture.supplyAsync(() -> {

                    chatId = update.getMessage().getChatId();
                    userLanguage = getUserLanguage(chatId);
                    final var department = managementRepository.findByName(update.getMessage().getText()).orElseThrow(NotFoundException::new);
                    final var managementEmployees = getManagementEmployees(department.getId());

                    if (userLanguage.equals("RU")) {
                        if (managementEmployees.isEmpty())
                            returnText = "Список пустой, Сотрудников этого Отдела нет в списке.";
                        else
                            returnText = "Выберите нужного Сотрудника из списка " + sighDown;
                        mainMenu = главное_Меню;
                    } else {
                        if (managementEmployees.isEmpty())
                            returnText = "Ro'yhat bo'sh, ushbu Bo'limdagi xodimlar ro'yxatda yo'q.";
                        else
                            returnText = "Ro'yxatdan kerakli xodimni tanlang " + sighDown;
                        mainMenu = bosh_Menu;
                    }

                    userRepository.updateUserStageByUserChatId(chatId, Stage.SELECTED_EMPLOYEE_NAME_FOR_SEARCH_ROLE_USER.name());
                    ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
                    List<KeyboardRow> keyboardRowList = new ArrayList<>();
                    replyKeyboardMarkup.setSelective(true);
                    replyKeyboardMarkup.setResizeKeyboard(true);
                    replyKeyboardMarkup.setOneTimeKeyboard(true);

                    for (Employee employee : managementEmployees) {
                        keyboardRowList.add(
                                new KeyboardRow(
                                        Collections.singletonList(
                                                KeyboardButton.builder()
                                                        .text(employee.getFullName())
                                                        .build()
                                        )
                                )
                        );
                    }

                    keyboardRowList.add(
                            new KeyboardRow(List.of(
                                    KeyboardButton.builder()
                                            .text(mainMenu)
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

    /***
     * USER role
     */
    public CompletableFuture<SendMessage> departmentEmployees(Update update) {
        return CompletableFuture.supplyAsync(() -> {

                    chatId = update.getMessage().getChatId();
                    userLanguage = getUserLanguage(chatId);
                    final var department = departmentRepository.findByName(update.getMessage().getText()).orElseThrow(NotFoundException::new);
                    final var departmentEmployees = getDepartmentEmployees(department.getId());

                    if (userLanguage.equals("RU")) {
                        if (departmentEmployees.isEmpty())
                            returnText = "Список пустой, Сотрудников этого Департамента нет в списке.";
                        else
                            returnText = "Выберите нужного Сотрудника из списка " + sighDown;
                        mainMenu = главное_Меню;
                    } else {
                        if (departmentEmployees.isEmpty())
                            returnText = "Ro'yhat bo'sh, ushbu Departamentdagi xodimlar ro'yxatda yo'q.";
                        else
                            returnText = "Ro'yxatdan kerakli xodimni tanlang " + sighDown;
                        mainMenu = bosh_Menu;
                    }

                    userRepository.updateUserStageByUserChatId(chatId, Stage.SELECTED_EMPLOYEE_NAME_FOR_SEARCH_ROLE_USER.name());
                    ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
                    List<KeyboardRow> keyboardRowList = new ArrayList<>();
                    replyKeyboardMarkup.setSelective(true);
                    replyKeyboardMarkup.setResizeKeyboard(true);
                    replyKeyboardMarkup.setOneTimeKeyboard(true);

                    for (Employee departmentEmployee : departmentEmployees) {
                        keyboardRowList.add(
                                new KeyboardRow(
                                        Collections.singletonList(
                                                KeyboardButton.builder()
                                                        .text(departmentEmployee.getFullName())
                                                        .build()
                                        )
                                )
                        );
                    }

                    keyboardRowList.add(
                            new KeyboardRow(List.of(
                                    KeyboardButton.builder()
                                            .text(mainMenu)
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

    /***
     * USER role
     */
    public CompletableFuture<SendMessage> departmentSectionUserRoleButtons(Update update) {
        return CompletableFuture.supplyAsync(() -> {

                    chatId = update.getMessage().getChatId();
                    userLanguage = getUserLanguage(chatId);
                    final var departmentNames = getDepartmentNames();

                    if (userLanguage.equals("RU")) {
                        if (departmentNames.isEmpty())
                            returnText = "Список пустой, Департаментов нет";
                        else
                            returnText = "Выберите нужный Департамент из списка " + sighDown;
                        mainMenu = главное_Меню;
                    } else {
                        if (departmentNames.isEmpty())
                            returnText = "Ro'yxat bo'sh, Departamentlar yo'q";
                        else
                            returnText = "Ro'yxatdan kerakli Departamentni tanlang " + sighDown;
                        mainMenu = bosh_Menu;
                    }

                    userRepository.updateUserStageByUserChatId(chatId, Stage.SECTION_SELECTED.name());
                    ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
                    List<KeyboardRow> keyboardRowList = new ArrayList<>();
                    replyKeyboardMarkup.setSelective(true);
                    replyKeyboardMarkup.setResizeKeyboard(true);
                    replyKeyboardMarkup.setOneTimeKeyboard(true);

                    for (String departmentName : departmentNames) {
                        keyboardRowList.add(
                                new KeyboardRow(
                                        Collections.singletonList(
                                                KeyboardButton.builder()
                                                        .text(departmentName)
                                                        .build()
                                        )
                                )
                        );
                    }

                    keyboardRowList.add(
                            new KeyboardRow(List.of(
                                    KeyboardButton.builder()
                                            .text(mainMenu)
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

    /***
     * USER role
     */
    public CompletableFuture<SendMessage> managementSectionUserRoleButtons(Update update) {
        return CompletableFuture.supplyAsync(() -> {

                    chatId = update.getMessage().getChatId();
                    userLanguage = getUserLanguage(chatId);
                    final var managementNames = getManagementNames();

                    if (userLanguage.equals("RU")) {
                        if (managementNames.isEmpty())
                            returnText = "Список пустой, отделов нет";
                        else
                            returnText = "Выберите нужный Отдел из списка " + sighDown;
                        mainMenu = главное_Меню;
                    } else {
                        if (managementNames.isEmpty())
                            returnText = "Ro'yxat bo'sh, Boshqarmalar yo'q";
                        else
                            returnText = "Ro'yxatdan kerakli Bo'limni tanlang " + sighDown;
                        mainMenu = bosh_Menu;
                    }

                    userRepository.updateUserStageByUserChatId(chatId, Stage.SECTION_SELECTED.name());
                    ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
                    List<KeyboardRow> keyboardRowList = new ArrayList<>();
                    replyKeyboardMarkup.setSelective(true);
                    replyKeyboardMarkup.setResizeKeyboard(true);
                    replyKeyboardMarkup.setOneTimeKeyboard(true);

                    for (String managementName : managementNames) {
                        keyboardRowList.add(
                                new KeyboardRow(
                                        Collections.singletonList(
                                                KeyboardButton.builder()
                                                        .text(managementName)
                                                        .build()
                                        )
                                )
                        );
                    }

                    keyboardRowList.add(
                            new KeyboardRow(List.of(
                                    KeyboardButton.builder()
                                            .text(mainMenu)
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

    /***
     * USER role
     */
    public CompletableFuture<SendMessage> positionSectionUserRoleButtons(Update update) {
        return CompletableFuture.supplyAsync(() -> {

                    chatId = update.getMessage().getChatId();
                    userLanguage = getUserLanguage(chatId);
                    final var positionNames = getPositionNames();

                    if (userLanguage.equals("RU")) {
                        if (positionNames.isEmpty())
                            returnText = "Список пустой, Должности нет";
                        else
                            returnText = "Выберите нужный Должность из списка " + sighDown;
                        mainMenu = главное_Меню;
                    } else {
                        if (positionNames.isEmpty())
                            returnText = "Ro'yxat bo'sh, Lavozimlar yo'q";
                        else
                            returnText = "Ro'yxatdan kerakli Lavozimni tanlang " + sighDown;
                        mainMenu = bosh_Menu;
                    }
                    userRepository.updateUserStageByUserChatId(chatId, Stage.SECTION_SELECTED.name());

                    ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
                    List<KeyboardRow> keyboardRowList = new ArrayList<>();
                    setPositionListToButtons(keyboardRowList, replyKeyboardMarkup);
                    keyboardRowList.add(
                            new KeyboardRow(
                                    List.of(
                                            KeyboardButton.builder()
                                                    .text(mainMenu)
                                                    .build()
                                    )
                            )
                    );
                    replyKeyboardMarkup.setKeyboard(keyboardRowList);

                    return SendMessage.builder()
                            .replyMarkup(replyKeyboardMarkup)
                            .text(returnText)
                            .chatId(chatId)
                            .build();
                }
        );
    }

    /***
     * USER role
     */
    public CompletableFuture<SendMessage> employeeSectionUserRoleButtons(Update update, String forWhat) {
        return CompletableFuture.supplyAsync(() -> {

                    chatId = update.getMessage().getChatId();
                    userLanguage = getUserLanguage(chatId);
                    final var employees = getEmployees();

                    if (userLanguage.equals("RU")) {
                        if (employees.isEmpty())
                            returnText = "Список пустой, Сотрудники нет";
                        else
                            returnText = "Введите имя и фамилию сотрудника " + sighDown;
                        mainMenu = главное_Меню;
                    } else {
                        if (employees.isEmpty())
                            returnText = "Ro'yxat bo'sh, Xodimlar yo'q";
                        else
                            returnText = "Xodimning ism familiyasini kiriting " + sighDown;
                        mainMenu = bosh_Menu;
                    }

                    userRepository.updateUserStageByUserChatId(chatId, Stage.SECTION_SELECTED.name());
                    ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
                    List<KeyboardRow> keyboardRowList = new ArrayList<>();
                    replyKeyboardMarkup.setSelective(true);
                    replyKeyboardMarkup.setResizeKeyboard(true);
                    replyKeyboardMarkup.setOneTimeKeyboard(true);

                    keyboardRowList.add(
                            new KeyboardRow(List.of(
                                    KeyboardButton.builder()
                                            .text(mainMenu)
                                            .build()
                            ))
                    );
                    replyKeyboardMarkup.setKeyboard(keyboardRowList);
                    if (forWhat.equals("forDeleting"))
                        userRepository.updateUserStageByUserChatId(chatId, Stage.ENTERED_EMPLOYEE_NAME_FOR_DELETE_ROLE_USER.name());
                    else
                        userRepository.updateUserStageByUserChatId(chatId, Stage.ENTERED_EMPLOYEE_NAME_FOR_SEARCH_ROLE_USER.name());
                    return SendMessage.builder()
                            .replyMarkup(replyKeyboardMarkup)
                            .chatId(String.valueOf(chatId))
                            .text(returnText)
                            .build();
                }
        );
    }

    /***
     * USER role
     */
    public CompletableFuture<SendMessage> findEmployeeSectionUserRoleButtons(Update update, String forWhat) {
        return CompletableFuture.supplyAsync(() -> {

                    chatId = update.getMessage().getChatId();
                    userLanguage = getUserLanguage(chatId);
                    final var employees = employeeRepository.findByFullNameIgnoreCaseContaining(
                            update.getMessage().getText()
                    );

                    if (userLanguage.equals("RU")) {
                        returnText = "Выберите нужного сотрудника из списка " + sighDown;
                        mainMenu = главное_Меню;
                    } else {
                        returnText = "Kerakli xodimni ro'yhatdan tanlang " + sighDown;
                        mainMenu = bosh_Menu;
                    }

                    ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
                    List<KeyboardRow> keyboardRowList = new ArrayList<>();
                    replyKeyboardMarkup.setSelective(true);
                    replyKeyboardMarkup.setResizeKeyboard(true);
                    replyKeyboardMarkup.setOneTimeKeyboard(true);

                    for (Employee employee : employees) {
                        final var fullName = employee.getFullName();
                        keyboardRowList.add(
                                new KeyboardRow(
                                        List.of(
                                                KeyboardButton.builder()
                                                        .text(fullName)
                                                        .build()
                                        )
                                )
                        );
                    }

                    keyboardRowList.add(
                            new KeyboardRow(
                                    Collections.singletonList(
                                            KeyboardButton.builder()
                                                    .text(mainMenu)
                                                    .build()
                                    )
                            )
                    );
                    replyKeyboardMarkup.setKeyboard(keyboardRowList);

                    if (forWhat.equals("forDeleting"))
                        userRepository.updateUserStageByUserChatId(chatId, Stage.SELECTED_EMPLOYEE_NAME_FOR_DELETING_ROLE_USER.name());
                    else
                        userRepository.updateUserStageByUserChatId(chatId, Stage.SELECTED_EMPLOYEE_NAME_FOR_SEARCH_ROLE_USER.name());

                    return SendMessage.builder()
                            .replyMarkup(replyKeyboardMarkup)
                            .chatId(String.valueOf(chatId))
                            .text(returnText)
                            .build();
                }
        );
    }

    /***
     * SUPER_ADMIN role
     */
    public CompletableFuture<SendMessage> superAdminButtons(Update update) {
        return CompletableFuture.supplyAsync(() -> {

                    chatId = update.getMessage().getChatId();
                    userLanguage = getUserLanguage(chatId);
                    String button1, button2, button3, button4, button5, languageButton;

                    if (userLanguage.equals("RU")) {
                        returnText = "Нажмите одну из следующих кнопок, чтобы выполнить следующее действие " + sighDown;
                        button1 = "Сотрудники";
                        button2 = "Должности";
                        button3 = "Департаменты";
                        button4 = "Отделы";
                        button5 = "Админы";
                        languageButton = "Изменить язык \uD83C\uDDF7\uD83C\uDDFA / \uD83C\uDDFA\uD83C\uDDFF";
                    } else {
                        returnText = "Keyingi amalni bajarish uchun quyidagi tugmalardan birini bosing " + sighDown;
                        button1 = "Xodimlar";
                        button2 = "Lavozimlar";
                        button3 = "Departamentlar";
                        button4 = "Boshqarmalar";
                        button5 = "Adminlar";
                        languageButton = "Tilni o'zgartirish \uD83C\uDDFA\uD83C\uDDFF / \uD83C\uDDF7\uD83C\uDDFA";
                    }

                    ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
                    List<KeyboardRow> keyboardRowList = new ArrayList<>();
                    replyKeyboardMarkup.setSelective(true);
                    replyKeyboardMarkup.setResizeKeyboard(true);
                    replyKeyboardMarkup.setOneTimeKeyboard(true);
                    keyboardRowList.add(
                            new KeyboardRow(
                                    List.of(
                                            KeyboardButton.builder()
                                                    .text(button1)
                                                    .build(),
                                            KeyboardButton.builder()
                                                    .text(button2)
                                                    .build()
                                    )
                            )
                    );
                    keyboardRowList.add(
                            new KeyboardRow(
                                    List.of(
                                            KeyboardButton.builder()
                                                    .text(button3)
                                                    .build(),
                                            KeyboardButton.builder()
                                                    .text(button4)
                                                    .build()
                                    )
                            )
                    );
                    keyboardRowList.add(
                            new KeyboardRow(
                                    List.of(
                                            KeyboardButton.builder()
                                                    .text(button5)
                                                    .build(),
                                            KeyboardButton.builder()
                                                    .text(languageButton)
                                                    .build()
                                    )
                            )
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

    /***
     * ADMIN role
     */
    public CompletableFuture<SendMessage> adminSectionAdminRoleButtons(Update update) {
        return CompletableFuture.supplyAsync(() -> {

                    chatId = update.getMessage().getChatId();
                    userLanguage = getUserLanguage(chatId);
                    String button1, button2;

                    if (userLanguage.equals("RU")) {
                        returnText = "Нажмите одну из следующих кнопок, чтобы выполнить следующее действие " + sighDown;
                        button1 = "Инфо Администратора";
                        button2 = "Список Админов ";
                        mainMenu = главное_Меню;
                    } else {
                        returnText = "Keyingi amalni bajarish uchun quyidagi tugmalardan birini bosing " + sighDown;
                        button1 = "Admin ma'lumotlari";
                        button2 = "Adminlar ro'yxati";
                        mainMenu = bosh_Menu;
                    }

                    ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
                    List<KeyboardRow> keyboardRowList = new ArrayList<>();
                    replyKeyboardMarkup.setSelective(true);
                    replyKeyboardMarkup.setResizeKeyboard(true);
                    replyKeyboardMarkup.setOneTimeKeyboard(true);
                    keyboardRowList.add(
                            new KeyboardRow(List.of(
                                    KeyboardButton.builder()
                                            .text(button1)
                                            .build(),
                                    KeyboardButton.builder()
                                            .text(button2)
                                            .build()
                            ))
                    );
                    keyboardRowList.add(
                            new KeyboardRow(List.of(
                                    KeyboardButton.builder()
                                            .text(mainMenu)
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

    /***
     * SUPER_ADMIN role
     */
    public CompletableFuture<SendMessage> adminSectionSuperAdminRoleButtons(Update update) {
        return CompletableFuture.supplyAsync(() -> {

                    chatId = update.getMessage().getChatId();
                    userLanguage = getUserLanguage(chatId);
                    String button1, button2, button3, button4;

                    if (userLanguage.equals("RU")) {
                        returnText = "Нажмите одну из следующих кнопок, чтобы выполнить следующее действие " + sighDown;
                        button1 = "Добавить Админ";
                        button2 = "Список Админов ";
                        button3 = "Инфо Администратора";
                        button4 = "Удалить Админ";
                        mainMenu = главное_Меню;
                    } else {
                        returnText = "Keyingi amalni bajarish uchun quyidagi tugmalardan birini bosing " + sighDown;
                        button1 = "Admin qo'shish";
                        button2 = "Adminlar ro'yxati";
                        button3 = "Admin ma'lumotlari";
                        button4 = "Admin o'chirish";
                        mainMenu = bosh_Menu;
                    }

                    ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
                    List<KeyboardRow> keyboardRowList = new ArrayList<>();
                    replyKeyboardMarkup.setSelective(true);
                    replyKeyboardMarkup.setResizeKeyboard(true);
                    replyKeyboardMarkup.setOneTimeKeyboard(true);
                    keyboardRowList.add(
                            new KeyboardRow(List.of(
                                    KeyboardButton.builder()
                                            .text(button1)
                                            .build(),
                                    KeyboardButton.builder()
                                            .text(button2)
                                            .build()
                            ))
                    );
                    keyboardRowList.add(
                            new KeyboardRow(List.of(
                                    KeyboardButton.builder()
                                            .text(button3)
                                            .build(),
                                    KeyboardButton.builder()
                                            .text(button4)
                                            .build()
                            ))
                    );
                    keyboardRowList.add(
                            new KeyboardRow(List.of(
                                    KeyboardButton.builder()
                                            .text(mainMenu)
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

    /***
     * ADMIN, SUPER_ADMIN roles
     */
    public CompletableFuture<SendMessage> employeeSectionButtons(Update update) {
        return CompletableFuture.supplyAsync(() -> {

                    chatId = update.getMessage().getChatId();
                    userLanguage = getUserLanguage(chatId);
                    String button1, button2, button3, button4;

                    if (userLanguage.equals("RU")) {
                        returnText = "Выберите нужное действие в разделе Сотрудники " + sighDown;
                        button1 = "Добавить Сотрудники";
                        button2 = "Найти Сотрудника";
                        button3 = "Редактировать Сотрудник";
                        button4 = "Удалить Сотрудрик";
                        mainMenu = главное_Меню;
                    } else {
                        returnText = "Xodimlar bo'limidagi kerakli amalni tanlang " + sighDown;
                        button1 = "Xodim qo'shish";
                        button2 = "Xodimni qidirish";
                        button3 = "Xodimni tahrirlash";
                        button4 = "Xodimni o'chirish";
                        mainMenu = bosh_Menu;
                    }
                    ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
                    List<KeyboardRow> keyboardRowList = new ArrayList<>();

                    replyKeyboardMarkup.setSelective(true);
                    replyKeyboardMarkup.setResizeKeyboard(true);
                    replyKeyboardMarkup.setOneTimeKeyboard(true);
                    keyboardRowList.add(
                            new KeyboardRow(List.of(
                                    KeyboardButton.builder()
                                            .text(button1)
                                            .build(),
                                    KeyboardButton.builder()
                                            .text(button2)
                                            .build()
                            ))
                    );
                    keyboardRowList.add(
                            new KeyboardRow(List.of(
                                    KeyboardButton.builder()
                                            .text(button3)
                                            .build(),
                                    KeyboardButton.builder()
                                            .text(button4)
                                            .build()
                            ))
                    );
                    keyboardRowList.add(
                            new KeyboardRow(List.of(
                                    KeyboardButton.builder()
                                            .text(mainMenu)
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

    /***
     * ADMIN, SUPER_ADMIN roles
     */
    public CompletableFuture<SendMessage> positionSectionButtons(Update update) {
        return CompletableFuture.supplyAsync(() -> {

                    chatId = update.getMessage().getChatId();
                    userLanguage = getUserLanguage(chatId);
                    String button1, button2, button3, button4;

                    if (userLanguage.equals("RU")) {
                        returnText = "Выберите нужное действие в разделе Должности " + sighDown;
                        button1 = "Добавить Должность";
                        button2 = "Список Должностов";
                        button3 = "Редактировать Должность";
                        button4 = "Удалить Должность";
                        mainMenu = главное_Меню;
                    } else {
                        returnText = "Lavozimlar bo'limidagi kerakli amalni tanlang " + sighDown;
                        button1 = "Lavozim qo'shish";
                        button2 = "Lavozimlar ro'yhati";
                        button3 = "Lavozimni tahrirlash";
                        button4 = "Lavozimni o'chirish";
                        mainMenu = bosh_Menu;
                    }

                    ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
                    List<KeyboardRow> keyboardRowList = new ArrayList<>();

                    replyKeyboardMarkup.setSelective(true);
                    replyKeyboardMarkup.setResizeKeyboard(true);
                    replyKeyboardMarkup.setOneTimeKeyboard(true);
                    keyboardRowList.add(
                            new KeyboardRow(List.of(
                                    KeyboardButton.builder()
                                            .text(button1)
                                            .build(),
                                    KeyboardButton.builder()
                                            .text(button2)
                                            .build()
                            ))
                    );
                    keyboardRowList.add(
                            new KeyboardRow(List.of(
                                    KeyboardButton.builder()
                                            .text(button3)
                                            .build(),
                                    KeyboardButton.builder()
                                            .text(button4)
                                            .build()
                            ))
                    );
                    keyboardRowList.add(
                            new KeyboardRow(List.of(
                                    KeyboardButton.builder()
                                            .text(mainMenu)
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

    /***
     * ADMIN, SUPER_ADMIN roles
     */
    public CompletableFuture<SendMessage> departmentSectionButtons(Update update) {
        return CompletableFuture.supplyAsync(() -> {

                    chatId = update.getMessage().getChatId();
                    userLanguage = getUserLanguage(chatId);
                    String button1, button2, button3, button4;

                    if (userLanguage.equals("RU")) {
                        returnText = "Выберите нужное действие в разделе Департаменты " + sighDown;
                        button1 = "Добавить Департамент";
                        button2 = "Список Департаменты";
                        button3 = "Редактировать Департамент";
                        button4 = "Удалить Департамент";
                        mainMenu = главное_Меню;
                    } else {
                        returnText = "Departamentlar bo'limidagi kerakli amalni tanlang " + sighDown;
                        button1 = "Departament qo'shish";
                        button2 = "Departamentlar ro'yhati";
                        button3 = "Departamentni tahrirlash";
                        button4 = "Departamentni o'chirish";
                        mainMenu = bosh_Menu;
                    }

                    ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
                    List<KeyboardRow> keyboardRowList = new ArrayList<>();

                    replyKeyboardMarkup.setSelective(true);
                    replyKeyboardMarkup.setResizeKeyboard(true);
                    replyKeyboardMarkup.setOneTimeKeyboard(true);
                    keyboardRowList.add(
                            new KeyboardRow(List.of(
                                    KeyboardButton.builder()
                                            .text(button1)
                                            .build(),
                                    KeyboardButton.builder()
                                            .text(button2)
                                            .build()
                            ))
                    );
                    keyboardRowList.add(
                            new KeyboardRow(List.of(
                                    KeyboardButton.builder()
                                            .text(button3)
                                            .build(),
                                    KeyboardButton.builder()
                                            .text(button4)
                                            .build()
                            ))
                    );
                    keyboardRowList.add(
                            new KeyboardRow(List.of(
                                    KeyboardButton.builder()
                                            .text(mainMenu)
                                            .build()
                            ))
                    );
                    replyKeyboardMarkup.setKeyboard(keyboardRowList);

                    return SendMessage.builder()
                            .replyMarkup(replyKeyboardMarkup)
                            .chatId(chatId)
                            .text(returnText)
                            .build();
                }
        );
    }

    /***
     * ADMIN, SUPER_ADMIN roles
     */
    public CompletableFuture<SendMessage> askNameForCreatingDepartment(Update update) {
        return CompletableFuture.supplyAsync(() -> {

                    chatId = update.getMessage().getChatId();
                    userLanguage = getUserLanguage(chatId);

                    if (userLanguage.equals("RU")) {
                        returnText = "Введите название для создания Департамента " + sighDown;
                        mainMenu = главное_Меню;
                    } else {
                        returnText = "Departament yaratish uchun nomini kiriting  " + sighDown;
                        mainMenu = bosh_Menu;
                    }

                    ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
                    List<KeyboardRow> keyboardRowList = new ArrayList<>();
                    replyKeyboardMarkup.setSelective(true);
                    replyKeyboardMarkup.setResizeKeyboard(true);
                    replyKeyboardMarkup.setOneTimeKeyboard(true);

                    keyboardRowList.add(
                            new KeyboardRow(List.of(
                                    KeyboardButton.builder()
                                            .text(mainMenu)
                                            .build()
                            ))
                    );

                    userRepository.updateUserStageByUserChatId(chatId, Stage.ENTER_NAME_FOR_CREATE_DEPARTMENT.name());
                    replyKeyboardMarkup.setKeyboard(keyboardRowList);

                    return SendMessage.builder()
                            .replyMarkup(replyKeyboardMarkup)
                            .chatId(chatId)
                            .text(returnText)
                            .build();
                }
        );
    }

    public CompletableFuture<SendMessage> askSelectDepartmentForCreateManagement(Update update) {
        return CompletableFuture.supplyAsync(() -> {

                    chatId = update.getMessage().getChatId();
                    userLanguage = getUserLanguage(chatId);

                    if (userLanguage.equals("UZ")) {
                        returnText = "Boshqarma yaratiladigan Departamentni tanlang";
                        mainMenu = bosh_Menu;
                    } else {
                        returnText = "Выберите Департамент, в котором будет создан Отдель";
                        mainMenu = главное_Меню;
                    }

                    ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
                    List<KeyboardRow> keyboardRowList = new ArrayList<>();
                    setDepartmentListToButtons(keyboardRowList, replyKeyboardMarkup);

                    keyboardRowList.add(
                            new KeyboardRow(List.of(
                                    KeyboardButton.builder()
                                            .text(mainMenu)
                                            .build()
                            ))
                    );
                    userRepository.updateUserStageByUserChatId(chatId, Stage.DEPARTMENT_SELECTED_FOR_CREATING_MANAGEMENT.name());
                    replyKeyboardMarkup.setKeyboard(keyboardRowList);

                    return SendMessage.builder()
                            .replyMarkup(replyKeyboardMarkup)
                            .chatId(chatId)
                            .text(returnText)
                            .build();

                }
        );
    }

    /***
     * ADMIN, SUPER_ADMIN roles
     */
    public CompletableFuture<SendMessage> getDepartmentListForDeleting(Update update) {
        return CompletableFuture.supplyAsync(() -> {

                    chatId = update.getMessage().getChatId();
                    userLanguage = getUserLanguage(chatId);

                    if (userLanguage.equals("RU")) {
                        returnText = "Выберите Департамент для удаления " + sighDown;
                        mainMenu = главное_Меню;
                    } else {
                        returnText = "O'chirish uchun Departamentni tanlang " + sighDown;
                        mainMenu = bosh_Menu;
                    }

                    ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
                    List<KeyboardRow> keyboardRowList = new ArrayList<>();
                    setDepartmentListToButtons(keyboardRowList, replyKeyboardMarkup);

                    keyboardRowList.add(
                            new KeyboardRow(List.of(
                                    KeyboardButton.builder()
                                            .text(mainMenu)
                                            .build()
                            ))
                    );

                    userRepository.updateUserStageByUserChatId(chatId, Stage.DEPARTMENT_SELECTED_FOR_DELETING.name());
                    replyKeyboardMarkup.setKeyboard(keyboardRowList);

                    return SendMessage.builder()
                            .replyMarkup(replyKeyboardMarkup)
                            .chatId(chatId)
                            .text(returnText)
                            .build();
                }
        );
    }

    /**
     * ADMIN, SUPER_ADMIN
     */
    public CompletableFuture<SendMessage> askSelectManagementForDeleting(Update update) {
        return CompletableFuture.supplyAsync(() -> {

                    chatId = update.getMessage().getChatId();
                    userLanguage = getUserLanguage(chatId);

                    if (userLanguage.equals("RU")) {
                        returnText = "Выберите Отдель для удаления " + sighDown;
                        mainMenu = главное_Меню;
                    } else {
                        returnText = "O'chirish uchun Boshqarmani tanlang " + sighDown;
                        mainMenu = bosh_Menu;
                    }

                    ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
                    List<KeyboardRow> keyboardRowList = new ArrayList<>();
                    setManagementListToButtons(keyboardRowList, replyKeyboardMarkup);

                    keyboardRowList.add(
                            new KeyboardRow(List.of(
                                    KeyboardButton.builder()
                                            .text(mainMenu)
                                            .build()
                            ))
                    );

                    userRepository.updateUserStageByUserChatId(chatId, Stage.MANAGEMENT_SELECTED_FOR_DELETING.name());
                    replyKeyboardMarkup.setKeyboard(keyboardRowList);

                    return SendMessage.builder()
                            .replyMarkup(replyKeyboardMarkup)
                            .chatId(chatId)
                            .text(returnText)
                            .build();
                }
        );

    }

    /**
     * ADMIN, SUPER_ADMIN roles
     */
    public CompletableFuture<SendMessage> askSelectManagementForUpdating(Department department, Update update) {
        return CompletableFuture.supplyAsync(() -> {

                    chatId = update.getMessage().getChatId();
                    userLanguage = getUserLanguage(chatId);

                    if (userLanguage.equals("RU")) {
                        returnText = "Выберите Отдель для редактирования " + sighDown;
                        mainMenu = главное_Меню;
                    } else {
                        returnText = "Tahrirlash uchun Boshqarmani tanlang " + sighDown;
                        mainMenu = bosh_Menu;
                    }

                    ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
                    List<KeyboardRow> keyboardRowList = new ArrayList<>();
                    setManagementListToButtonsByDepartmentId(department.getId(), keyboardRowList, replyKeyboardMarkup);

                    keyboardRowList.add(
                            new KeyboardRow(
                                    List.of(
                                            KeyboardButton.builder()
                                                    .text(mainMenu)
                                                    .build()
                                    )
                            )
                    );

                    userRepository.updateUserStageByUserChatId(chatId, Stage.MANAGEMENT_SELECTED_FOR_UPDATING.name());
                    replyKeyboardMarkup.setKeyboard(keyboardRowList);

                    return SendMessage.builder()
                            .replyMarkup(replyKeyboardMarkup)
                            .chatId(chatId)
                            .text(returnText)
                            .build();
                }
        );
    }


    /**
     * ADMIN, SUPER_ADMIN roles
     */
    public CompletableFuture<SendMessage> getDepartmentListForUpdating(Update update) {
        return CompletableFuture.supplyAsync(() -> {

                    chatId = update.getMessage().getChatId();
                    userLanguage = getUserLanguage(chatId);

                    if (userLanguage.equals("RU")) {
                        returnText = "Выберите Департамент для редактирования " + sighDown;
                        mainMenu = главное_Меню;
                    } else {
                        returnText = "Tahrirlash uchun Departamentni tanlang " + sighDown;
                        mainMenu = bosh_Menu;
                    }

                    ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
                    List<KeyboardRow> keyboardRowList = new ArrayList<>();
                    replyKeyboardMarkup.setSelective(true);
                    replyKeyboardMarkup.setResizeKeyboard(true);
                    replyKeyboardMarkup.setOneTimeKeyboard(true);

                    for (String departmentName : getDepartmentNames()) {
                        keyboardRowList.add(
                                new KeyboardRow(
                                        Collections.singletonList(
                                                KeyboardButton.builder()
                                                        .text(departmentName)
                                                        .build()
                                        )
                                )
                        );
                    }
                    keyboardRowList.add(
                            new KeyboardRow(List.of(
                                    KeyboardButton.builder()
                                            .text(mainMenu)
                                            .build()
                            ))
                    );

                    userRepository.updateUserStageByUserChatId(chatId, Stage.DEPARTMENT_SELECTED_FOR_UPDATING.name());
                    replyKeyboardMarkup.setKeyboard(keyboardRowList);

                    return SendMessage.builder()
                            .replyMarkup(replyKeyboardMarkup)
                            .chatId(chatId)
                            .text(returnText)
                            .build();
                }
        );
    }

    /**
     * ADMIN, SUPER_ADMIN roles
     */
    public CompletableFuture<SendMessage> askNameForUpdatingDepartment(Update update) {
        return CompletableFuture.supplyAsync(() -> {

                    chatId = update.getMessage().getChatId();
                    userLanguage = getUserLanguage(chatId);

                    if (userLanguage.equals("RU")) {
                        returnText = "Чтобы изменить название Департамента, введите новое имя " + sighDown;
                        mainMenu = главное_Меню;
                    } else {
                        returnText = "Departament nomini tahrirlash uchun yangi nom kiriting  " + sighDown;
                        mainMenu = bosh_Menu;
                    }

                    ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
                    List<KeyboardRow> keyboardRowList = new ArrayList<>();
                    replyKeyboardMarkup.setSelective(true);
                    replyKeyboardMarkup.setResizeKeyboard(true);
                    replyKeyboardMarkup.setOneTimeKeyboard(true);

                    keyboardRowList.add(
                            new KeyboardRow(
                                    List.of(
                                            KeyboardButton.builder()
                                                    .text(mainMenu)
                                                    .build()
                                    )
                            )
                    );

                    userRepository.updateUserStageByUserChatId(chatId, Stage.ENTER_NAME_FOR_UPDATE_DEPARTMENT.name());
                    replyKeyboardMarkup.setKeyboard(keyboardRowList);

                    return SendMessage.builder()
                            .replyMarkup(replyKeyboardMarkup)
                            .chatId(chatId)
                            .text(returnText)
                            .build();
                }
        );
    }

    /**
     * ADMIN, SUPER_ADMIN roles
     */
    public CompletableFuture<SendMessage> managementSectionButtons(Update update) {
        return CompletableFuture.supplyAsync(() -> {

                    chatId = update.getMessage().getChatId();
                    userLanguage = getUserLanguage(chatId);
                    String button1, button2, button3, button4;

                    if (userLanguage.equals("RU")) {
                        returnText = "Выберите нужное действие в разделе Отделы " + sighDown;
                        button1 = "Добавить Отдел";
                        button2 = "Список Отделы";
                        button3 = "Редактировать Отдел";
                        button4 = "Удалить Отдел";
                        mainMenu = главное_Меню;
                    } else {
                        returnText = "Boshqarmalar bo'limidagi kerakli amalni tanlang " + sighDown;
                        button1 = "Boshqarma qo'shish";
                        button2 = "Boshqarmalar ro'yhati";
                        button3 = "Boshqarmalarni tahrirlash";
                        button4 = "Boshqarmani o'chirish";
                        mainMenu = bosh_Menu;
                    }

                    ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
                    List<KeyboardRow> keyboardRowList = new ArrayList<>();

                    replyKeyboardMarkup.setSelective(true);
                    replyKeyboardMarkup.setResizeKeyboard(true);
                    replyKeyboardMarkup.setOneTimeKeyboard(true);
                    keyboardRowList.add(
                            new KeyboardRow(List.of(
                                    KeyboardButton.builder()
                                            .text(button1)
                                            .build(),
                                    KeyboardButton.builder()
                                            .text(button2)
                                            .build()
                            ))
                    );
                    keyboardRowList.add(
                            new KeyboardRow(List.of(
                                    KeyboardButton.builder()
                                            .text(button3)
                                            .build(),
                                    KeyboardButton.builder()
                                            .text(button4)
                                            .build()
                            ))
                    );
                    keyboardRowList.add(
                            new KeyboardRow(List.of(
                                    KeyboardButton.builder()
                                            .text(mainMenu)
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

    /**
     * ADMIN, SUPER_ADMIN roles
     */
    public CompletableFuture<SendMessage> askingNameForCreatingManagement(Update update, String forWhat) {
        return CompletableFuture.supplyAsync(() -> {

                    chatId = update.getMessage().getChatId();
                    userLanguage = getUserLanguage(chatId);

                    if (userLanguage.equals("RU")) {
                        returnText = "Введите имя для Отдела" + sighDown;
                        mainMenu = главное_Меню;
                    } else {
                        returnText = "Boshqarma uchun nom kiriting  " + sighDown;
                        mainMenu = bosh_Menu;
                    }

                    ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
                    List<KeyboardRow> keyboardRowList = new ArrayList<>();
                    replyKeyboardMarkup.setSelective(true);
                    replyKeyboardMarkup.setResizeKeyboard(true);
                    replyKeyboardMarkup.setOneTimeKeyboard(true);

                    keyboardRowList.add(
                            new KeyboardRow(List.of(
                                    KeyboardButton.builder()
                                            .text(mainMenu)
                                            .build()
                            ))
                    );

                    if (forWhat.equals("forSaving"))
                        userRepository.updateUserStageByUserChatId(chatId, Stage.ENTER_NAME_FOR_SAVING_UPDATED_MANAGEMENT.name());
                    else
                        userRepository.updateUserStageByUserChatId(chatId, Stage.ENTER_NAME_FOR_CREATE_MANAGEMENT.name());

                    replyKeyboardMarkup.setKeyboard(keyboardRowList);

                    return SendMessage.builder()
                            .replyMarkup(replyKeyboardMarkup)
                            .chatId(chatId)
                            .text(returnText)
                            .build();
                }
        );
    }

    private String getUserLanguage(Long userChatId) {
        return userRepository.getUserLanguageByUserChatId(userChatId);
    }

    public List<String> getDepartmentNames() {
        List<String> depertmentNameList = new ArrayList<>();
        for (Department department : departmentService.getDepartmentList()) {
            depertmentNameList.add(department.getName());
        }
        return depertmentNameList;
    }

    private List<Employee> getDepartmentEmployees(Long departmentId) {
        return employeeRepository.getEmployeesByPosition_Management_Department_Id(departmentId);
    }

    private List<Employee> getManagementEmployees(Long departmentId) {
        return employeeRepository.getEmployeesByPosition_Management_Id(departmentId);
    }

    private List<Employee> getPositionEmployees(Long position_id) {
        return employeeRepository.getEmployeesByPosition_Id(position_id);
    }

    public void setDepartmentListToButtons(List<KeyboardRow> keyboardRowList, ReplyKeyboardMarkup replyKeyboardMarkup) {

        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);

        for (String departmentName : getDepartmentNames()) {
            keyboardRowList.add(
                    new KeyboardRow(
                            List.of(
                                    KeyboardButton.builder()
                                            .text(departmentName)
                                            .build()
                            )
                    )
            );
        }
    }

    public void setManagementListToButtons(List<KeyboardRow> keyboardRowList, ReplyKeyboardMarkup replyKeyboardMarkup) {

        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);

        for (String managementName : getManagementNames()) {
            keyboardRowList.add(
                    new KeyboardRow(
                            List.of(
                                    KeyboardButton.builder()
                                            .text(managementName)
                                            .build()
                            )
                    )
            );
        }
    }

    public void setPositionListToButtons(List<KeyboardRow> keyboardRowList, ReplyKeyboardMarkup replyKeyboardMarkup) {

        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);

        List<KeyboardRow> keyboardRows = getPositionNames().stream()
                .map(positionName -> new KeyboardRow(
                                List.of(
                                        KeyboardButton.builder()
                                                .text(positionName)
                                                .build()
                                )
                        )
                )
                .toList();
        keyboardRowList.addAll(keyboardRows);
    }

    public void setPositionListToButtonsByManagementId(Long managementId, List<KeyboardRow> keyboardRowList, ReplyKeyboardMarkup replyKeyboardMarkup) {

        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        final var namesByManagementId = getPositionNamesByManagementId(managementId);

        List<KeyboardRow> newKeyboardRows = namesByManagementId.stream()
                .map(positionName -> new KeyboardRow(
                                List.of(
                                        KeyboardButton.builder()
                                                .text(positionName)
                                                .build()
                                )
                        )
                )
                .toList();
        keyboardRowList.addAll(newKeyboardRows);
    }

    public void setManagementListToButtonsByDepartmentId(Long departmentId, List<KeyboardRow> keyboardRowList, ReplyKeyboardMarkup replyKeyboardMarkup) {

        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        final var namesByDepartmentId = getManagementNamesByDepartmentId(departmentId);

        for (String managementName : namesByDepartmentId) {
            keyboardRowList.add(
                    new KeyboardRow(
                            List.of(
                                    KeyboardButton.builder()
                                            .text(managementName)
                                            .build()
                            )
                    )
            );
        }
    }

    public void setAdminListToButtons(List<KeyboardRow> keyboardRowList, ReplyKeyboardMarkup replyKeyboardMarkup) {

        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);

        for (User admin : adminList()) {
            keyboardRowList.add(
                    new KeyboardRow(
                            List.of(
                                    KeyboardButton.builder()
                                            .text(admin.getRole() + " - " + admin.getPhoneNumber())
                                            .build()
                            )
                    )
            );
        }
    }

    private List<String> getDepartmentEmployeesNames(Long departmentId) {
        return getDepartmentEmployees(departmentId)
                .stream()
                .map(Employee::getFullName)
                .collect(Collectors.toList());
    }

    private List<String> getManagementNames() {
        return managementRepository.getManagementNames();
    }

    private List<String> getManagementNamesByDepartmentId(Long departmentId) {
        return managementRepository.getManagementNamesByDepartmentId(departmentId);
    }

    private List<String> getPositionNames() {
        return positionRepository.getPositionNames();
    }

    private List<String> getPositionNamesByManagementId(Long id) {
        return positionRepository.getPositionNamesByManagementId(id);
    }

    public List<Employee> getEmployees() {
        return employeeRepository.findAll();
    }

    public List<User> adminList() {
        return userRepository.getAdminList();
    }

    public CompletableFuture<SendMessage> getAdminList(Update update) {
        return CompletableFuture.supplyAsync(() -> {

                    chatId = update.getMessage().getChatId();
                    userLanguage = getUserLanguage(chatId);

                    ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
                    List<KeyboardRow> keyboardRowList = new ArrayList<>();
                    replyKeyboardMarkup.setSelective(true);
                    replyKeyboardMarkup.setResizeKeyboard(true);
                    replyKeyboardMarkup.setOneTimeKeyboard(true);
                    final var adminList = adminList();
                    returnText = "List of Admins";

                    if (userLanguage.equals("UZ")) {
                        mainMenu = bosh_Menu;
                    } else {
                        mainMenu = главное_Меню;
                    }
                    for (User admin : adminList) {
                        keyboardRowList.add(
                                new KeyboardRow(
                                        List.of(
                                                KeyboardButton.builder()
                                                        .text(admin.getRole() + " - " + admin.getPhoneNumber())
                                                        .build()
                                        )
                                )
                        );
                    }

                    keyboardRowList.add(
                            new KeyboardRow(
                                    Collections.singletonList(
                                            KeyboardButton.builder()
                                                    .text(mainMenu)
                                                    .build()
                                    )
                            )
                    );

                    replyKeyboardMarkup.setKeyboard(keyboardRowList);

                    return SendMessage.builder()
                            .replyMarkup(replyKeyboardMarkup)
                            .chatId(chatId)
                            .text(returnText)
                            .build();
                }
        );
    }

    public CompletableFuture<SendMessage> askSelectDepartmentForUpdatingManagement(Update update, String forWhat) {
        return CompletableFuture.supplyAsync(() -> {

                    chatId = update.getMessage().getChatId();
                    userLanguage = getUserLanguage(chatId);
                    List<KeyboardRow> keyboardRowList = new ArrayList<>();
                    ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();

                    if (userLanguage.equals("RU")) {

                        if (forWhat.equals("forSelecting"))
                            returnText = "Выберите Департамент Отдела для редактирования " + sighDown;
                        else
                            returnText = "Выберите Департамент для Отдела " + sighDown;
                        mainMenu = главное_Меню;

                    } else {

                        if (forWhat.equals("forSelecting"))
                            returnText = "Tahrirlanadigan Boshqarma departamentini tanlang " + sighDown;
                        else
                            returnText = "Boshqarma uchun Departamentni tanlang " + sighDown;

                        mainMenu = bosh_Menu;
                    }
                    setDepartmentListToButtons(keyboardRowList, replyKeyboardMarkup);

                    keyboardRowList.add(
                            new KeyboardRow(
                                    List.of(
                                            KeyboardButton.builder()
                                                    .text(mainMenu)
                                                    .build()
                                    )
                            )
                    );

                    if (forWhat.equals("forSaving"))
                        userRepository.updateUserStageByUserChatId(chatId, Stage.DEPARTMENT_SELECTED_FOR_SAVING_UPDATED_MANAGEMENT.name());
                    else
                        userRepository.updateUserStageByUserChatId(chatId, Stage.DEPARTMENT_SELECTED_FOR_UPDATING_MANAGEMENT.name());
                    replyKeyboardMarkup.setKeyboard(keyboardRowList);

                    return SendMessage.builder()
                            .replyMarkup(replyKeyboardMarkup)
                            .chatId(chatId)
                            .text(returnText)
                            .build();
                }
        );
    }

    public CompletableFuture<SendMessage> askSelectPositionForDeleting(Update update) {
        return CompletableFuture.supplyAsync(() -> {

                    chatId = update.getMessage().getChatId();
                    userLanguage = getUserLanguage(chatId);

                    if (userLanguage.equals("RU")) {
                        returnText = "Выберите Должность для удаления " + sighDown;
                        mainMenu = главное_Меню;
                    } else {
                        returnText = "O'chirish uchun Lavozimni tanlang " + sighDown;
                        mainMenu = bosh_Menu;
                    }

                    ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
                    List<KeyboardRow> keyboardRowList = new ArrayList<>();
                    setPositionListToButtons(keyboardRowList, replyKeyboardMarkup);

                    keyboardRowList.add(
                            new KeyboardRow(List.of(
                                    KeyboardButton.builder()
                                            .text(mainMenu)
                                            .build()
                            ))
                    );

                    userRepository.updateUserStageByUserChatId(chatId, Stage.POSITION_SELECTED_FOR_DELETING.name());
                    replyKeyboardMarkup.setKeyboard(keyboardRowList);

                    return SendMessage.builder()
                            .replyMarkup(replyKeyboardMarkup)
                            .chatId(chatId)
                            .text(returnText)
                            .build();
                }
        );
    }

    public CompletableFuture<SendMessage> askSelectManagementForCreatingPosition(Update update, String forWhat) {
        return CompletableFuture.supplyAsync(() -> {

                    chatId = update.getMessage().getChatId();
                    userLanguage = getUserLanguage(chatId);

                    if (userLanguage.equals("UZ")) {

                        if (forWhat.equals("forCreating"))
                            returnText = "Lavozim yaratiladigan Boshqarmani tanlang " + sighDown;

                        else if (forWhat.equals("forCreatingEmployee"))
                            returnText = "Xodim qo'shiladigan Boshqarmani tanlang " + sighDown;

                        else
                            returnText = "Lavozim tahrirlanadigan Boshqarmani tanlang " + sighDown;

                        mainMenu = bosh_Menu;
                    } else {

                        if (forWhat.equals("forCreating"))
                            returnText = "Выберите отдела, в которой будет создана Должность " + sighDown;

                        else if (forWhat.equals("forCreatingEmployee")) {
                            returnText = "Выберите отдела, в которую будет добавлен сотрудник " + sighDown;

                        } else
                            returnText = "Выберите отдела, в которой будет редактирована Должность " + sighDown;

                        mainMenu = главное_Меню;
                    }

                    List<KeyboardRow> keyboardRowList = new ArrayList<>();
                    ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
                    setManagementListToButtons(keyboardRowList, replyKeyboardMarkup);

                    keyboardRowList.add(
                            new KeyboardRow(
                                    Collections.singleton(
                                            KeyboardButton.builder()
                                                    .text(mainMenu)
                                                    .build()
                                    )
                            )
                    );
                    replyKeyboardMarkup.setKeyboard(keyboardRowList);

                    if (forWhat.equals("forCreating"))
                        userRepository.updateUserStageByUserChatId(chatId, Stage.MANAGEMENT_SELECTED_FOR_CREATING_POSITION.name());
                    else if (forWhat.equals("forCreatingEmployee"))
                        userRepository.updateUserStageByUserChatId(chatId, Stage.MANAGEMENT_SELECTED_FOR_CREATING_EMPLOYEE.name());
                    else
                        userRepository.updateUserStageByUserChatId(chatId, Stage.MANAGEMENT_SELECTED_FOR_UPDATING_POSITION.name());

                    return SendMessage.builder()
                            .chatId(chatId)
                            .text(returnText)
                            .replyMarkup(replyKeyboardMarkup)
                            .build();
                }
        );
    }

    public CompletableFuture<SendMessage> askNameForCreatingPosition(Update update, String forWhat) {
        return CompletableFuture.supplyAsync(() -> {

                    chatId = update.getMessage().getChatId();
                    userLanguage = getUserLanguage(chatId);

                    if (userLanguage.equals("RU")) {
                        returnText = "Введите название должности" + sighDown;
                        mainMenu = главное_Меню;
                    } else {
                        returnText = "Lavozim uchun nom kiriting  " + sighDown;
                        mainMenu = bosh_Menu;
                    }

                    ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
                    List<KeyboardRow> keyboardRowList = new ArrayList<>();
                    replyKeyboardMarkup.setSelective(true);
                    replyKeyboardMarkup.setResizeKeyboard(true);
                    replyKeyboardMarkup.setOneTimeKeyboard(true);

                    keyboardRowList.add(
                            new KeyboardRow(
                                    List.of(
                                            KeyboardButton.builder()
                                                    .text(mainMenu)
                                                    .build()
                                    )
                            )
                    );

                    if (forWhat.equals("forCreating"))
                        userRepository.updateUserStageByUserChatId(chatId, Stage.ENTER_NAME_FOR_CREATING_POSITION_NAME.name());
                    else
                        userRepository.updateUserStageByUserChatId(chatId, Stage.ENTER_NAME_FOR_UPDATE_POSITION.name());

                    replyKeyboardMarkup.setKeyboard(keyboardRowList);

                    return SendMessage.builder()
                            .replyMarkup(replyKeyboardMarkup)
                            .chatId(chatId)
                            .text(returnText)
                            .build();
                }
        );
    }

    public CompletableFuture<SendMessage> askSelectPositionForUpdating(Management prevManagement, Update update, String forWhat) {
        return CompletableFuture.supplyAsync(() -> {

                    chatId = update.getMessage().getChatId();
                    userLanguage = getUserLanguage(chatId);
                    ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
                    List<KeyboardRow> keyboardRowList = new ArrayList<>();
                    setPositionListToButtonsByManagementId(prevManagement.getId(), keyboardRowList, replyKeyboardMarkup);

                    if (userLanguage.equals("RU")) {
                        if (forWhat.equals("forCreatingEmployee")) {
                            returnText = "Выберите должность, чтобы добавить сотрудника";

                            keyboardRowList.add(
                                    new KeyboardRow(
                                            List.of(
                                                    KeyboardButton.builder()
                                                            .text(mainMenu)
                                                            .build(),
                                                    KeyboardButton.builder()
                                                            .text("Создать должность")
                                                            .build()
                                            )
                                    )
                            );
                        } else {
                            returnText = "Выберите Должность для редактирования " + sighDown;

                            keyboardRowList.add(
                                    new KeyboardRow(
                                            List.of(
                                                    KeyboardButton.builder()
                                                            .text(mainMenu)
                                                            .build()
                                            )
                                    )
                            );
                        }
                        mainMenu = главное_Меню;
                    } else {
                        if (forWhat.equals("forCreatingEmployee")) {
                            returnText = "Xodim qo'shish uchun Lavozim tanlang";

                            keyboardRowList.add(
                                    new KeyboardRow(
                                            List.of(
                                                    KeyboardButton.builder()
                                                            .text(mainMenu)
                                                            .build(),
                                                    KeyboardButton.builder()
                                                            .text("Lavozim yaratish")
                                                            .build()
                                            )
                                    )
                            );
                        } else {
                            returnText = "Tahrirlash uchun Lavozimni tanlang " + sighDown;

                            keyboardRowList.add(
                                    new KeyboardRow(
                                            List.of(
                                                    KeyboardButton.builder()
                                                            .text(mainMenu)
                                                            .build()
                                            )
                                    )
                            );
                        }
                        mainMenu = bosh_Menu;
                    }

                    if (forWhat.equals("forCreatingEmployee")) {
                        userRepository.updateUserStageByUserChatId(chatId, Stage.POSITION_FOR_CREATING_EMPLOYEE.name());
                        userRepository.updateUserStepByUserChatId(chatId, "personalInfo");
                    } else
                        userRepository.updateUserStageByUserChatId(chatId, Stage.POSITION_SELECTED_FOR_UPDATING.name());
                    replyKeyboardMarkup.setKeyboard(keyboardRowList);

                    return SendMessage.builder()
                            .replyMarkup(replyKeyboardMarkup)
                            .chatId(chatId)
                            .text(returnText)
                            .build();
                }
        );
    }

    public CompletableFuture<SendMessage> adminInfoForSUPER_ADMIN(Update update) {
        return CompletableFuture.supplyAsync(() -> {

                    chatId = update.getMessage().getChatId();
                    final var text = update.getMessage().getText();
                    final var substring = text.substring(text.length() - 12);
                    final var user = userRepository.findByPhoneNumber(substring).orElseThrow();
                    final var adminInfo = setAdminInfo(user);

                    final var sendMessageCompletableFuture = getAdminList(update);
                    final var sendMessage = sendMessageCompletableFuture.join();
                    final var replyMarkup = sendMessage.getReplyMarkup();

                    return SendMessage.builder()
                            .replyMarkup(replyMarkup)
                            .text(adminInfo)
                            .chatId(chatId)
                            .build();
                }
        );
    }

    public String setAdminInfo(User user) {
        return "Role:  " + user.getRole() +
                "\nPhone Number:  " + user.getPhoneNumber() +
                "\nChat Id:  " + user.getUserChatId() +
                "\nUsername:  @" + user.getUserName() +
                "\nAdded at:  " + user.getCreatedAt();
    }

    public CompletableFuture<SendMessage> askSelectAdminForDeleting(Update update) {
        return CompletableFuture.supplyAsync(() -> {

                    chatId = update.getMessage().getChatId();
                    userLanguage = getUserLanguage(chatId);

                    if (userLanguage.equals("UZ")) {
                        returnText = "O'chirish uchun Adminni tanlang";
                        mainMenu = bosh_Menu;
                    } else {
                        returnText = "Выберите Админстратора для удаления";
                        mainMenu = главное_Меню;
                    }

                    List<KeyboardRow> keyboardRowList = new ArrayList<>();
                    ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
                    setAdminListToButtons(keyboardRowList, replyKeyboardMarkup);
                    replyKeyboardMarkup.setKeyboard(keyboardRowList);

                    keyboardRowList.add(
                            new KeyboardRow(
                                    Collections.singletonList(
                                            new KeyboardButton(
                                                    mainMenu
                                            )
                                    )
                            )
                    );

                    userRepository.updateUserStageByUserChatId(chatId, Stage.ADMIN_SELECTED_FOR_DELETING.name());
                    replyKeyboardMarkup.setKeyboard(keyboardRowList);

                    return SendMessage.builder()
                            .replyMarkup(replyKeyboardMarkup)
                            .text(returnText)
                            .chatId(chatId)
                            .build();
                }
        );
    }

    public CompletableFuture<SendMessage> askPhoneNumberForAddingAdmin(Update update) {
        return CompletableFuture.supplyAsync(() -> {

                    chatId = update.getMessage().getChatId();
                    userLanguage = getUserLanguage(chatId);

                    if (userLanguage.equals("UZ")) {
                        mainMenu = bosh_Menu;
                        returnText = """
                                ADMIN yaratish uchun uning telefon raqamini kiriting
                                                                
                                ‼️ Namuna: 991234567
                                99 - Aloqa operatori maxsus kodi;
                                1234567 - Mobil raqam;
                                """;
                    } else {
                        mainMenu = главное_Меню;
                        returnText = """
                                Для создания АДМИНА введите его номер телефона
                                                                
                                ‼️ Образец:  991234567
                                99 – Спецкод оператора связи;
                                1234567 – Номер мобильного;
                                """;
                    }
                    List<KeyboardRow> keyboardRowList = new ArrayList<>();
                    ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
                    replyKeyboardMarkup.setSelective(true);
                    replyKeyboardMarkup.setResizeKeyboard(true);
                    replyKeyboardMarkup.setOneTimeKeyboard(true);
                    keyboardRowList.add(
                            new KeyboardRow(
                                    Collections.singletonList(
                                            KeyboardButton.builder()
                                                    .text(mainMenu)
                                                    .build()
                                    )
                            )
                    );

                    userRepository.updateUserStageByUserChatId(chatId, Stage.ENTER_PHONE_NUMBER_FOR_CREATING_ADMIN.name());
                    replyKeyboardMarkup.setKeyboard(keyboardRowList);

                    return SendMessage.builder()
                            .replyMarkup(replyKeyboardMarkup)
                            .text(returnText)
                            .chatId(chatId)
                            .build();
                }
        );
    }

    public CompletableFuture<SendMessage> askConfirmationForDeletingEmployee(Update update) {
        return CompletableFuture.supplyAsync(() -> {

                    chatId = update.getMessage().getChatId();
                    userLanguage = getUserLanguage(chatId);
                    String confirmationButton, cancelButton;
                    final var employee = employeeRepository.findByFullName(update.getMessage().getText()).orElseThrow();

                    if (userLanguage.equals("UZ")) {
                        returnText = getEmployeeInfoForUserLanguage_UZ(employee);
                        confirmationButton = "Tasdiqlash ✅";
                        cancelButton = "Bekor qilish ❌";
                    } else {
                        returnText = getEmployeeInfoForUserLanguage_UZ(employee); // to do russian language info
                        confirmationButton = "Потвердить ✅";
                        cancelButton = "Отменить ❌";
                    }

                    List<KeyboardRow> keyboardRowList = new ArrayList<>();
                    ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
                    replyKeyboardMarkup.setSelective(true);
                    replyKeyboardMarkup.setResizeKeyboard(true);
                    replyKeyboardMarkup.setOneTimeKeyboard(true);

                    keyboardRowList.add(
                            new KeyboardRow(
                                    List.of(KeyboardButton.builder()
                                                    .text(cancelButton)
                                                    .build(),
                                            KeyboardButton.builder()
                                                    .text(confirmationButton)
                                                    .build()
                                    )
                            )
                    );
                    replyKeyboardMarkup.setKeyboard(keyboardRowList);

                    userRepository.updateUserStageByUserChatId(chatId, Stage.CONFIRMATION_FOR_DELETING_EMPLOYEE.name());

                    return SendMessage.builder()
                            .replyMarkup(replyKeyboardMarkup)
                            .text(returnText)
                            .chatId(chatId)
                            .build();
                }
        );
    }


    public String getEmployeeInfoForUserLanguage_UZ(Employee employee) {
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

    public String getEmployeeSkills(Employee employee) {

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

    public String getEmployeeEducationsInfo(Employee employee) {

        String educationInfo = "";
        final var educations = employee.getEducations();
        final var educationsIds = employeeRepository.getEmployeeEducationsIds(employee.getId());

        for (Education ignored : educations) {

            int value, preValue = 0;
            StringBuilder stringBuilder = new StringBuilder();

            for (Education education : educationRepository.findEducationByIdIn(educationsIds)) {
                value = education.getType().getValue();

                if (!(value < preValue))
                    educationInfo = String.valueOf(stringBuilder.append(setEduInfos(education)).append(" "));

                preValue = value;
            }
        }

        return educationInfo;
    }

    public String checkCommas(String string) {

        String substringed = "";
        if (string.endsWith(", ")) {
            final var length = string.length();
            substringed = string.substring(0, length - 2);
        }

        return substringed;
    }

    public String setEduInfos(Education education) {
        return "\nTa'lim muassasi: " + education.getName() +
                "\nTa'lim yo'nalishi: " + education.getEducationField() +
                "\n" + education.getType() +
                "\nMuddatlari: (" + education.getStartedDate() + " - " + education.getEndDate() + ")\n";
    }

    public CompletableFuture<SendMessage> cancelledConfirmation(Update update) {
        return CompletableFuture.supplyAsync(() -> {

                    chatId = update.getMessage().getChatId();
                    userLanguage = getUserLanguage(chatId);

                    if (userLanguage.equals("UZ"))
                        returnText = "O'chirish bekor qilindi!";
                    else
                        returnText = "Удаление отменено!";

                    final var messageCompletableFuture = employeeSectionButtons(update);
                    final var sendMessage = messageCompletableFuture.join();
                    final var replyMarkup = sendMessage.getReplyMarkup();

                    return SendMessage.builder()
                            .replyMarkup(replyMarkup)
                            .text(returnText)
                            .chatId(chatId)
                            .build();
                }
        );
    }

    public CompletableFuture<SendMessage> askPositionNameForCreatingEmployee(Update update) {
        return CompletableFuture.supplyAsync(() -> {
                    chatId = update.getMessage().getChatId();
                    userLanguage = getUserLanguage(chatId);
                    String cancelButton;

                    if (userLanguage.equals("UZ")) {
                        returnText = "Lavozim uchun nom kiriting";
                        cancelButton = "Bekor qilish";
                    } else {
                        returnText = "Введите название должности";
                        cancelButton = "Отменить";
                    }

                    List<KeyboardRow> keyboardRowList = new ArrayList<>();
                    ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
                    replyKeyboardMarkup.setSelective(true);
                    replyKeyboardMarkup.setResizeKeyboard(true);
                    replyKeyboardMarkup.setOneTimeKeyboard(true);

                    keyboardRowList.add(
                            new KeyboardRow(
                                    Collections.singletonList(
                                            KeyboardButton.builder()
                                                    .text(cancelButton)
                                                    .build()
                                    )
                            )
                    );

                    userRepository.updateUserStageByUserChatId(chatId, Stage.ENTERED_POSITION_NAME_FOR_CREATING_EMPLOYEE.name());
                    replyKeyboardMarkup.setKeyboard(keyboardRowList);

                    return SendMessage.builder()
                            .replyMarkup(replyKeyboardMarkup)
                            .text(returnText)
                            .chatId(chatId)
                            .build();
                }
        );
    }

    public CompletableFuture<SendMessage> askInformationOfEmployeeForCreating(Update update, String step) {
        return CompletableFuture.supplyAsync(() -> {

                    chatId = update.getMessage().getChatId();
                    userLanguage = getUserLanguage(chatId);
                    String stopButton = "";

                    if (userLanguage.equals("UZ"))
                        stopButton = "To'xtatish 🛑";
                    else
                        stopButton = "Остановить 🛑";

                    ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
                    List<KeyboardRow> keyboardRowList = new ArrayList<>();
                    replyKeyboardMarkup.setOneTimeKeyboard(true);
                    replyKeyboardMarkup.setResizeKeyboard(true);
                    replyKeyboardMarkup.setSelective(true);

                    final var userStepsByStage = getUserStepsByStage(update, moveToNext);
                    SendMessage sendMessage = userStepsByStage.join();
                    final var text = sendMessage.getText();

                    if (text.equals("Ta'lim bosqichini tanlang ⬇️") || text.equals("Выберите уровень образования: ⬇️")) {
                        final var eduTypes = EduType.values();

                        for (int i = 0; i < eduTypes.length; i += 2) {
                            EduType eduType1 = eduTypes[i];
                            EduType eduType2 = (i + 1 < eduTypes.length) ? eduTypes[i + 1] : null;

                            KeyboardButton button1 = KeyboardButton.builder()
                                    .text(eduType1.name())
                                    .build();
                            KeyboardButton button2 = KeyboardButton.builder()
                                    .text(Objects.requireNonNull(eduType2).name())
                                    .build();

                            keyboardRowList.add(
                                    new KeyboardRow(
                                            List.of(button1, button2)
                                    )
                            );
                        }

                    } else if (text.equals("Kiritish uchun rasm yoki resumeni tanlang ⬇️") || text.equals("Выберите изображение или резюме для вложения ⬇️")) {
                        final var fileTypes = FileType.values();
                        int totalFileTypes = fileTypes.length;

                        for (int i = 0; i < totalFileTypes; i += 2) {
                            FileType fileType1 = fileTypes[i];
                            FileType fileType2 = (i + 1 < totalFileTypes) ? fileTypes[i + 1] : null;

                            KeyboardButton button1 = KeyboardButton.builder()
                                    .text(fileType1.name())
                                    .build();
                            KeyboardButton button2 = (fileType2 != null) ? KeyboardButton.builder()
                                    .text(fileType2.name())
                                    .build() : null;

                            keyboardRowList.add(
                                    new KeyboardRow(
                                            List.of(button1, Objects.requireNonNull(button2)
                                            )
                                    )
                            );
                        }

                        if (totalFileTypes % 2 == 1) {
                            FileType lastFileType = fileTypes[totalFileTypes - 1];
                            keyboardRowList.add(
                                    new KeyboardRow(
                                            Collections.singleton(
                                                    KeyboardButton.builder()
                                                            .text(lastFileType.name())
                                                            .build()
                                            )
                                    )
                            );

                            keyboardRowList.add(new KeyboardRow(
                                            Collections.singleton(
                                                    KeyboardButton.builder()
                                                            .text(lastFileType.name())
                                                            .build())
                                    )
                            );
                        }
                    }
                    keyboardRowList.add(
                            new KeyboardRow(
                                    List.of(
                                            KeyboardButton.builder()
                                                    .text(stopButton)
                                                    .build()
                                    )
                            )
                    );
                    replyKeyboardMarkup.setKeyboard(keyboardRowList);

                    return SendMessage.builder()
                            .replyMarkup(replyKeyboardMarkup)
                            .chatId(chatId)
                            .text(text)
                            .build();
                }
        );
    }

    public CompletableFuture<SendMessage> getUserStepsByStage(Update update, boolean moveToNext) {
        return CompletableFuture.supplyAsync(() -> {

                    String messageText = "";
                    chatId = update.getMessage().getChatId();
                    userLanguage = getUserLanguage(chatId);

                    if (userLanguage.equals("UZ"))
                        messageText = getSteps_uz(userStageIndex);
                    else
                        messageText = getSteps_ru(userStageIndex);

                    if (moveToNext) {
                        if (userStageIndex == 2) {
                            String userInputDate = update.getMessage().getText();

                            if (isValidDateFormat(userInputDate))
                                incrementUserStage();
                            else {
                                if (userLanguage.equals("UZ"))
                                    messageText = """
                                            ❌Sana noto'g'ri kiritilgan. Sanani to'g'ri kiriting:

                                            ❗️Namuna: 1999-12-31 (yyyy-mm-dd)""";
                                else
                                    messageText = """
                                            ❌Дата введен неверно. Введите правильную дату:

                                            ❗Образец: 1999-12-31 (yyyy-mm-dd)""";
                            }
                        } else if (userStageIndex == 7) {
                            String userInputDate = update.getMessage().getText();
                            final var checkedEduPeriod = checkEduPeriod(userInputDate);

                            if (checkedEduPeriod)
                                incrementUserStage();
                            else {
                                if (userLanguage.equals("UZ"))
                                    messageText = """
                                            ❌Muddat oralig'i noto'g'ri kiritildi. Sana formatini to'g'ri kiriting:

                                            ❗️Namuna: 2018-2022;
                                            ❗️Agar hozirda davom etayotgan bo'lsa: 2020-Present""";
                                else
                                    messageText = """
                                            ❌Неверно введен сроки выполнения. Введите правильный формат даты:
                                                                                         
                                            ❗️Образец: 2018-2022;
                                            ❗️Если в настоящее время продолжается: (с 2020 г. по настоящее время)""";
                            }
                        } else
                            incrementUserStage();
                    } else
                        decrementUserStage();

                    switch (userStageIndex) {
                        case 0 ->
                                userRepository.updateUserStepByUserChatId(chatId, Stage.ENTERED_EMPLOYEE_NAME_ROLE_ADMIN.name());
                        case 1 ->
                                userRepository.updateUserStepByUserChatId(chatId, Stage.ENTERED_EMPLOYEE_BIRTHDATE_ROLE_ADMIN.name());
                        case 2 ->
                                userRepository.updateUserStepByUserChatId(chatId, Stage.ENTERED_EMPLOYEE_PHONE_NUMBER_ROLE_ADMIN.name());
                        case 3 -> userRepository.updateUserStepByUserChatId(chatId, Stage.ENTERED_EMPLOYEE_NATIONALITY.name());
                        case 4 ->
                                userRepository.updateUserStepByUserChatId(chatId, Stage.SELECTED_EMPLOYEE_EDUCATION_TYPE.name());
                        case 5 ->
                                userRepository.updateUserStepByUserChatId(chatId, Stage.ENTERED_EMPLOYEE_EDUCATION_NAME.name());
                        case 6 ->
                                userRepository.updateUserStepByUserChatId(chatId, Stage.ENTERED_EMPLOYEE_EDUCATION_FIELD.name());
                        case 7 -> userRepository.updateUserStepByUserChatId(chatId, Stage.ENTERED_EMPLOYEE_SKILLS.name());
                        case 8 ->
                                userRepository.updateUserStepByUserChatId(chatId, Stage.ENTERED_EMPLOYEE_EDUCATION_PERIOD.name());
                        case 9 -> userRepository.updateUserStepByUserChatId(chatId, Stage.SELECTED_EMPLOYEE_FILE_TYPE.name());
                    }

                    return SendMessage.builder()
                            .text(messageText)
                            .chatId(chatId)
                            .build();
                }
        );
    }

    private boolean isValidDateFormat(String inputDate) {

        String[] parts = inputDate.split("-");

        if (parts.length != 3) {
            System.out.println("Invalid format. Please use yyyy-MM-dd.");
            return false;
        }

        try {
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            int day = Integer.parseInt(parts[2]);

            LocalDate birthdate = LocalDate.of(year, month, day);
            LocalDate currentDate = LocalDate.now();

            if (birthdate.isBefore(currentDate)) {
                System.out.println("Valid birthdate.");
                return true;
            } else {
                System.out.println("Invalid birthdate.");
                return false;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid number format.");
            return false;
        } catch (Exception e) {
            System.out.println("Invalid date values.");
            return false;
        }
    }

    private boolean checkEduPeriod(String period) {
        boolean isValuable;
        String[] years = period.split("-");

        if (years.length != 2)
            return false;

        try {
            int startYear = Integer.parseInt(years[0]);
            int endYear;
            boolean isPresent = years[1].equalsIgnoreCase("Present");

            if (isPresent) {
                endYear = Year.now().getValue();
            } else {
                endYear = Integer.parseInt(years[1]);
            }

            int currentYear = Year.now().getValue();

            if (startYear <= currentYear && (isPresent || (endYear >= startYear && endYear <= currentYear))) {
                System.out.println("Valid education time period.");
                isValuable = true;
            } else {
                System.out.println("Invalid education time period.");
                isValuable = false;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid year format.");
            isValuable = false;
        }

        return isValuable;
    }

    private void incrementUserStage() {
        if (userStageIndex < steps_uz.size() - 1) {
            userStageIndex++;
        }
    }

    private void decrementUserStage() {
        if (userStageIndex > 0)
            userStageIndex--;
    }

    public void retryUserSteps() {
        userStageIndex = 0;
    }
}