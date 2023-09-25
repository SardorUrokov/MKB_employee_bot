package com.example.mkb_employee_bot.service;

import com.example.mkb_employee_bot.entity.*;
import com.example.mkb_employee_bot.entity.dto.ManagementDTO;
import com.example.mkb_employee_bot.entity.dto.PositionDTO;
import com.example.mkb_employee_bot.entity.enums.FileType;
import com.example.mkb_employee_bot.entity.enums.Role;
import com.example.mkb_employee_bot.repository.*;
import com.example.mkb_employee_bot.entity.enums.Stage;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import jakarta.ws.rs.NotFoundException;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class BotService {

    private final UserService userService;
    private final FileService fileService;
    private final ButtonService buttonService;
    private final PositionServiceImpl positionService;
    private final EmployeeServiceImpl employeeService;
    private final DepartmentServiceImpl departmentService;
    private final ManagementServiceImpl managementService;

    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final PositionRepository positionRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final ManagementRepository managementRepository;

    private Long chatId;
    private String userLanguage;
    private String returnText = "";

    public void registerUser(Update update) {
        CompletableFuture.runAsync(() -> {

                    Long chatId = update.getMessage().getChatId();
                    final var user = update.getMessage().getFrom();
                    final var userName = user.getUserName();
                    final var firstName = user.getFirstName();
                    final var lastName = user.getLastName() == null ? ("") : user.getLastName();
                    final var fullName = firstName + " " + lastName;

                    Optional<User> optionalUser = userRepository.findByUserChatId(chatId);
                    if (optionalUser.isEmpty()) {
                        userService.register(
                                User.builder()
                                        .userChatId(chatId)
                                        .fullName(fullName)
                                        .userName(userName)
                                        .build()
                        );
                    }
                }
        );
    }

    public CompletableFuture<SendMessage> setUserLanguage(Update update) {
        return CompletableFuture.supplyAsync(() -> {
                    registerUser(update);

                    final var updateMessage = update.getMessage();
                    chatId = update.getMessage().getChatId();
                    String buttonText, language;

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
                            new KeyboardRow(
                                    Collections.singleton(
                                            KeyboardButton.builder()
                                                    .requestContact(true)
                                                    .text(buttonText)
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

    public void setPhoneNumber(Update update) {
        CompletableFuture.runAsync(() -> {

                    String phoneNumber = update.getMessage().getContact().getPhoneNumber();
                    chatId = update.getMessage().getChatId();
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

    public CompletableFuture<SendPhoto> getSelectedEmployeeInfo(Update update) {
        return CompletableFuture.supplyAsync(() -> {

                    chatId = update.getMessage().getChatId();
                    userLanguage = getUserLanguage(chatId);
                    Employee employee = employeeRepository.findByFullNameAndDeletedFalse(update.getMessage().getText()).orElseThrow(NotFoundException::new);

                    final var info = userLanguage.equals("UZ")
                            ? buttonService.getEmployeeInfoForUserLanguage_UZ(employee)
                            : buttonService.getEmployeeInfoForUserLanguage_RU(employee);

                    final CompletableFuture<SendMessage> messageCompletableFuture;
                    if (userRepository.getUserRoleByUserChatId(chatId).equals("USER"))
                        messageCompletableFuture = buttonService.employeeSectionUserRoleButtons(update, "");
                    else
                        messageCompletableFuture = buttonService.employeeSectionButtons(update);

                    AppPhoto employeePhoto = null;
                    for (AppPhoto appPhoto : employee.getAppPhotos()) {
                        if (appPhoto.getFileType().name().equals(FileType.EMPLOYEE_PHOTO.name())) {
                            employeePhoto = appPhoto;
                            break;
                        }
                    }

                    if (employeePhoto == null) {

                        java.io.File photoFile;
                        photoFile = new File("C:\\Users\\user\\IdeaProjects\\MKBank Projects\\MKB_employee_bot\\src\\main\\resources\\mkb_Logo.jpg");
                        InputFile photoInputFile = new InputFile(photoFile);
                        userRepository.updateUserStageByUserChatId(chatId, Stage.STARTED.name());
                        final var sendMessage = messageCompletableFuture.join();
                        final var replyMarkup = sendMessage.getReplyMarkup();

                        return SendPhoto.builder()
                                .replyMarkup(replyMarkup)
                                .chatId(chatId)
                                .photo(photoInputFile)
                                .caption(info)
                                .build();
                    } else {

                        byte[] fileAsArrayOfBytes = employeePhoto.getFileAsArrayOfBytes();
                        InputStream inputStream = new ByteArrayInputStream(fileAsArrayOfBytes);
                        InputFile photoInputFile = new InputFile(inputStream, "photo.jpg");

                        userRepository.updateUserStageByUserChatId(chatId, Stage.STARTED.name());
                        final var sendMessage = messageCompletableFuture.join();
                        final var replyMarkup = sendMessage.getReplyMarkup();

                        return SendPhoto.builder()
                                .replyMarkup(replyMarkup)
                                .chatId(chatId)
                                .photo(photoInputFile)
                                .caption(info)
                                .build();
                    }
                }
        );
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

    public CompletableFuture<SendMessage> getAdminInfo(Update update) {
        return CompletableFuture.supplyAsync(() -> {

                    chatId = update.getMessage().getChatId();
                    userLanguage = getUserLanguage(chatId);
                    final CompletableFuture<SendMessage> messageCompletableFuture;
                    final var user = userRepository.findByUserChatId(chatId).orElseThrow();
                    final var adminInfo = buttonService.setAdminInfo(user);
                    final var userRole = getUserRole(chatId);

                    if (userRole.equals("SUPER_ADMIN"))
                        messageCompletableFuture = buttonService.adminSectionSuperAdminRoleButtons(update);
                    else
                        messageCompletableFuture = buttonService.adminSectionAdminRoleButtons(update);

                    SendMessage sendMessage = messageCompletableFuture.join();
                    final var replyMarkup = sendMessage.getReplyMarkup();

                    return SendMessage.builder()
                            .replyMarkup(replyMarkup)
                            .chatId(chatId)
                            .text(adminInfo)
                            .build();
                }
        );
    }


    /**
     * DEPARTMENT methods
     */
    public CompletableFuture<SendMessage> createDepartment(Update update) {
        return CompletableFuture.supplyAsync(() -> {

                    chatId = update.getMessage().getChatId();
                    userLanguage = getUserLanguage(chatId);
                    final var department = departmentService.createDepartment(update.getMessage().getText());

                    if (userLanguage.equals("UZ"))
                        returnText = department.getName() + " nomli Departament " + department.getId() + "-id bilan saqlandi";
                    else
                        returnText = "Департамент с именем " + department.getName() + " сохранен с " + department.getId() + " id";

                    final var messageCompletableFuture = buttonService.departmentSectionButtons(update);
                    final var replyMarkup = messageCompletableFuture.join().getReplyMarkup();

                    return SendMessage.builder()
                            .replyMarkup(replyMarkup)
                            .chatId(chatId)
                            .text(returnText)
                            .build();
                }
        );
    }

    public CompletableFuture<SendMessage> updateDepartment(Update update, Department previousDepartment) {
        return CompletableFuture.supplyAsync(() -> {

                    chatId = update.getMessage().getChatId();
                    userLanguage = getUserLanguage(chatId);
                    final var updatedDepartment = departmentService.updateDepartment(previousDepartment.getName(), update.getMessage().getText());

                    if (userLanguage.equals("UZ"))
                        returnText = previousDepartment.getName() + " nomli Departament " + updatedDepartment.getName() + " ga o'zgartirildi";
                    else
                        returnText = "Название Департамента " + previousDepartment.getName() + " изменено на " + updatedDepartment.getName();

                    final var messageCompletableFuture = buttonService.departmentSectionButtons(update);
                    final var replyMarkup = messageCompletableFuture.join().getReplyMarkup();

                    return SendMessage.builder()
                            .replyMarkup(replyMarkup)
                            .chatId(chatId)
                            .text(returnText)
                            .build();
                }
        );
    }

    public CompletableFuture<SendMessage> deleteDepartment(Update update) {
        return CompletableFuture.supplyAsync(() -> {

                    chatId = update.getMessage().getChatId();
                    final var text = update.getMessage().getText();
                    departmentService.deleteDepartment(text);

                    userLanguage = getUserLanguage(chatId);
                    if (userLanguage.equals("UZ"))
                        returnText = text + " Departementi o'chirildi";
                    else
                        returnText = "Департамент " + text + " был удален";


                    final var messageCompletableFuture = buttonService.departmentSectionButtons(update);
                    final var replyMarkup = messageCompletableFuture.join().getReplyMarkup();

                    return SendMessage.builder()
                            .replyMarkup(replyMarkup)
                            .chatId(chatId)
                            .text(returnText)
                            .build();
                }
        );
    }


    /**
     * MANAGEMENT methods
     */
    public CompletableFuture<SendMessage> createManagement(Department selectedDepartment, Update update) {
        return CompletableFuture.supplyAsync(() -> {

                    chatId = update.getMessage().getChatId();
                    userLanguage = getUserLanguage(chatId);

                    ManagementDTO dto = ManagementDTO.builder()
                            .departmentId(selectedDepartment.getId())
                            .name(update.getMessage().getText())
                            .build();
                    final var management = managementService.createManagement(dto);

                    if (userLanguage.equals("UZ"))
                        returnText = management.getName() + " nomli Boshqarma " + management.getId() + "-id bilan saqlandi";
                    else
                        returnText = "Отдель с именем " + management.getName() + " сохранен с " + management.getId() + " id";

                    final var messageCompletableFuture = buttonService.managementSectionButtons(update);
                    final var replyMarkup = messageCompletableFuture.join().getReplyMarkup();

                    return SendMessage.builder()
                            .replyMarkup(replyMarkup)
                            .chatId(chatId)
                            .text(returnText)
                            .build();
                }
        );
    }

    public CompletableFuture<SendMessage> updateManagement(Update update, Management previousManagement) {
        return CompletableFuture.supplyAsync(() -> {

                    chatId = update.getMessage().getChatId();
                    userLanguage = getUserLanguage(chatId);
                    ManagementDTO managementDTO = new ManagementDTO(
                            previousManagement.getDepartment().getId(),
                            update.getMessage().getText()
                    );
                    final var updatedDepartment = managementService.updateManagement(previousManagement.getId(), managementDTO);

                    if (userLanguage.equals("UZ"))
                        returnText = previousManagement.getName() + " nomli Boshqarma " + updatedDepartment.getName() + " ga o'zgartirildi";
                    else
                        returnText = "Название Отдела " + previousManagement.getName() + " изменено на " + updatedDepartment.getName();

                    final var messageCompletableFuture = buttonService.managementSectionButtons(update);
                    final var replyMarkup = messageCompletableFuture.join().getReplyMarkup();

                    return SendMessage.builder()
                            .replyMarkup(replyMarkup)
                            .chatId(chatId)
                            .text(returnText)
                            .build();
                }
        );
    }

    public CompletableFuture<SendMessage> deleteManagement(Update update) {
        return CompletableFuture.supplyAsync(() -> {

                    chatId = update.getMessage().getChatId();
                    final var text = update.getMessage().getText();
                    managementService.deleteManagement(text);

                    if (getUserLanguage(chatId).equals("UZ"))
                        returnText = text + " Boshqarmasi o'chirildi";
                    else
                        returnText = "Отдель " + text + " был удален";

                    final var messageCompletableFuture = buttonService.managementSectionButtons(update);
                    final var replyMarkup = messageCompletableFuture.join().getReplyMarkup();

                    return SendMessage.builder()
                            .replyMarkup(replyMarkup)
                            .chatId(chatId)
                            .text(returnText)
                            .build();
                }
        );
    }


    /**
     * POSITION methods
     */
    public CompletableFuture<SendMessage> createPosition(Management management, Update update) {
        return CompletableFuture.supplyAsync(() -> {

                    chatId = update.getMessage().getChatId();
                    userLanguage = getUserLanguage(chatId);

                    PositionDTO dto = PositionDTO.builder()
                            .managementId(management.getId())
                            .name(update.getMessage().getText())
                            .build();

                    final var position = positionService.createPosition(dto);

                    if (userLanguage.equals("UZ"))
                        returnText = position.getName() + " nomli Lavozim " + position.getId() + "-id bilan saqlandi";
                    else
                        returnText = "Должность с именем " + position.getName() + " сохранен с " + position.getId() + " id";

                    final var messageCompletableFuture = buttonService.positionSectionButtons(update);
                    final var replyMarkup = messageCompletableFuture.join().getReplyMarkup();

                    return SendMessage.builder()
                            .replyMarkup(replyMarkup)
                            .chatId(chatId)
                            .text(returnText)
                            .build();
                }
        );
    }

    public CompletableFuture<SendMessage> updatePosition(Update update, Position previousPosition) {
        return CompletableFuture.supplyAsync(() -> {

                    chatId = update.getMessage().getChatId();
                    userLanguage = getUserLanguage(chatId);
                    PositionDTO positionDTO = new PositionDTO(
                            update.getMessage().getText(),
                            previousPosition.getManagement().getId()
                    );

                    final var updatedPosition = positionService.updatePosition(previousPosition.getId(), positionDTO);
                    final var messageCompletableFuture = buttonService.positionSectionButtons(update);
                    final var replyMarkup = messageCompletableFuture.join().getReplyMarkup();

                    if (userLanguage.equals("UZ"))
                        returnText = previousPosition.getName() + " nomli Lavozim " + updatedPosition.getName() + " ga o'zgartirildi";
                    else
                        returnText = "Название Должность " + previousPosition.getName() + " изменено на " + updatedPosition.getName();

                    return SendMessage.builder()
                            .replyMarkup(replyMarkup)
                            .chatId(chatId)
                            .text(returnText)
                            .build();
                }
        );
    }

    public CompletableFuture<SendMessage> deletePosition(Update update) {
        return CompletableFuture.supplyAsync(() -> {

                    chatId = update.getMessage().getChatId();
                    final var text = update.getMessage().getText();
                    positionService.deletePosition(text);

                    if (getUserLanguage(chatId).equals("UZ"))
                        returnText = text + " lavozimi o'chirildi";
                    else
                        returnText = "Должность " + text + " был удален";

                    final var messageCompletableFuture = buttonService.positionSectionButtons(update);
                    final var replyMarkup = messageCompletableFuture.join().getReplyMarkup();

                    return SendMessage.builder()
                            .replyMarkup(replyMarkup)
                            .chatId(chatId)
                            .text(returnText)
                            .build();
                }
        );
    }

    public CompletableFuture<SendMessage> deleteAdmin(Update update) {
        return CompletableFuture.supplyAsync(() -> {

                    chatId = update.getMessage().getChatId();
                    userLanguage = getUserLanguage(chatId);
                    final var text = update.getMessage().getText();
                    final var substring = text.substring(text.length() - 12);
                    final var isUserDeleted = userService.deleteUser(substring);

                    if (isUserDeleted) {
                        if (userLanguage.equals("UZ"))
                            returnText = substring + " raqamli User ADMINlar listidan o'chirildi";
                        else
                            returnText = "Пользователь номер " + substring + " удален из списка АДМИНов.";
                    } else {
                        if (userLanguage.equals("UZ"))
                            returnText = "⚠️ Tizimda SUPER_ADMIN soni 1ta bo'lganligi uchun User Adminlar ro'yxatidan o'chirilmadi ‼️";
                        else
                            returnText = "⚠️ Поскольку количество SUPER_ADMIN в системе было 1, он не был удален из списка администраторов пользователей ‼️";
                    }
                    final var messageCompletableFuture = buttonService.adminSectionSuperAdminRoleButtons(update);
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

    public CompletableFuture<SendMessage> createAdmin(Update update) {
        return CompletableFuture.supplyAsync(() -> {

                    chatId = update.getMessage().getChatId();
                    userLanguage = getUserLanguage(chatId);
                    final var updateText = update.getMessage().getText();

                    if (updateText.length() == 9) {
                        final var phoneNumber = "998" + updateText;
                        User user = new User(phoneNumber, Role.ADMIN);
                        userService.register(user);

                        if (userLanguage.equals("UZ"))
                            returnText = updateText + " telefon raqami bilan ADMIN yaratildi✅";
                        else
                            returnText = "✅ АДМИН создан с номером телефона " + updateText;

                        final var messageCompletableFuture = buttonService.adminSectionSuperAdminRoleButtons(update);
                        final var sendMessage = messageCompletableFuture.join();
                        final var replyMarkup = sendMessage.getReplyMarkup();
                        userRepository.updateUserStageByUserChatId(chatId, Stage.ADMIN_CREATED.name());

                        return SendMessage.builder()
                                .replyMarkup(replyMarkup)
                                .text(returnText)
                                .chatId(chatId)
                                .build();
                    } else {
                        if (userLanguage.equals("UZ"))
                            returnText = """
                                    Telefon raqam noto'g'ri formatda kiritildi ‼️

                                    Raqamni qaytadan kiriting
                                    """;
                        else
                            returnText = """
                                    Номер телефона введен в неверном формате ‼️

                                    Введите номер повторно
                                    """;

                        final var messageCompletableFuture = buttonService.askPhoneNumberForAddingAdmin(update);
                        final var sendMessage = messageCompletableFuture.join();
                        final var replyMarkup = sendMessage.getReplyMarkup();

                        return SendMessage.builder()
                                .replyMarkup(replyMarkup)
                                .text(returnText)
                                .chatId(chatId)
                                .build();
                    }
                }
        );
    }

    public CompletableFuture<SendMessage> changeLanguage(Update update) {
        return CompletableFuture.supplyAsync(() -> {

                    chatId = update.getMessage().getChatId();
                    userLanguage = getUserLanguage(chatId);

                    if (userLanguage.equals("UZ")) {
                        userRepository.updateLanguageByUserId(chatId, "RU");
                        returnText = "Язык изменен 🇺🇿 > 🇷🇺";
                    } else {
                        userRepository.updateLanguageByUserId(chatId, "UZ");
                        returnText = "Til o'zgartirildi 🇷🇺 > 🇺🇿";
                    }

                    final var role = userRepository.getUserRoleByUserChatId(chatId);
                    final CompletableFuture<SendMessage> messageCompletableFuture;

                    if (role.equals("USER"))
                        messageCompletableFuture = buttonService.userButtons(update);
                    else
                        messageCompletableFuture = buttonService.superAdminButtons(update);

                    SendMessage sendMessage = messageCompletableFuture.join();
                    final var replyMarkup = sendMessage.getReplyMarkup();

                    return SendMessage.builder()
                            .replyMarkup(replyMarkup)
                            .text(returnText)
                            .chatId(chatId)
                            .build();
                }
        );
    }

    public CompletableFuture<SendMessage> deleteEmployee(Employee deletingEmployee, Update update) {
        return CompletableFuture.supplyAsync(() -> {

                    chatId = update.getMessage().getChatId();
                    userLanguage = getUserLanguage(chatId);
                    employeeService.deleteEmployee(deletingEmployee.getId());

                    if (userLanguage.equals("UZ"))
                        returnText = "Xodim " + deletingEmployee.getFullName().toUpperCase() + " ro'yxatdan olib tashlandi";
                    else
                        returnText = "Сотрудник " + deletingEmployee.getFullName().toUpperCase() + " удален из списка";

                    final var messageCompletableFuture = buttonService.employeeSectionButtons(update);
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

    public CompletableFuture<SendMessage> createEmployee(Employee creatingEmployee, Update update) {
        return CompletableFuture.supplyAsync(() -> {

                    chatId = update.getMessage().getChatId();
                    userLanguage = getUserLanguage(chatId);
                    final var employee = employeeService.createEmployee(creatingEmployee);
                    if (employee.getAppPhotos() != null)
                        fileService.saveEmployee_Photo(employee);

                    if (userLanguage.equals("UZ"))
                        returnText = employee.getPosition().getName().toUpperCase() + " lavozimli xodim " + employee.getFullName().toUpperCase() + "  " + employee.getId() + "-id bilan saqlandi";
                    else
                        returnText = "Сотрудник " + employee.getFullName().toUpperCase() + " с должностом " + employee.getPosition().getName().toUpperCase() + " сохранен с " + employee.getId() + " id";

                    final var messageCompletableFuture = buttonService.employeeSectionButtons(update);
                    final var replyMarkup = messageCompletableFuture.join().getReplyMarkup();
                    userRepository.updateUserStageByUserChatId(chatId, Stage.STARTED.name());
                    userRepository.updateUserStepByUserChatId(chatId, "");
                    buttonService.retryUserSteps();

                    return SendMessage.builder()
                            .replyMarkup(replyMarkup)
                            .text(returnText)
                            .chatId(chatId)
                            .build();
                }
        );
    }

    public CompletableFuture<SendMessage> updateEmployee(Update update, Employee updatingEmployee) {
        return CompletableFuture.supplyAsync(() -> {

                    chatId = update.getMessage().getChatId();
                    userLanguage = getUserLanguage(chatId);
                    final var text = update.getMessage().getText();

                    if ("Bekor qilish ❌".equals(text) || "Отменить ❌".equals(text)) {

                        userRepository.updateUserStepByUserChatId(chatId, "cancelled");
                        userRepository.updateUserStageByUserChatId(chatId, Stage.STARTED.name());
                        if (userLanguage.equals("UZ"))
                            returnText = "Tahrirlash to'xtatildi ❗️";
                        else
                            returnText = "Редактирование остановлено ❗️";

                    } else {
                        final var userStep = userRepository.getUserStepByUserChatId(chatId);

                        switch (userStep) {
                            case "fullname":
                                updatingEmployee.setFullName(text);
                                break;
                            case "phoneNumber":
                                updatingEmployee.setPhoneNumber(text);
                                break;
                            case "dateOfBirth":
                                updatingEmployee.setDateOfBirth(text);
                                break;
                            case "nationality":
                                updatingEmployee.setNationality(text);
                                break;
                            case "position":
//                            updatingEmployee.setPosition();
                                buttonService.askSelectManagementForCreatingPosition(update, "");
                                break;
                            case "eduName":
//                            updatingEmployee.getEducations() //the first iterate edu s and set name
                                break;
                            case "eduField":

                                break;
                            case "eduType":

                                break;
                            case "eduPeriod":

                                break;
                            case "skills":
                                final var skills = updatingEmployee.getSkills();
                                skills.add(skillRepository.save(new Skill(text)));
                                updatingEmployee.setSkills(skills);
                                break;
                            case "attachments":

                                break;
                        }
                        userRepository.updateUserStepByUserChatId(chatId, "");
                        final var updatedEmployee = employeeService.updateEmployee(updatingEmployee);

                        returnText = switch (userLanguage) {
                            case "UZ" -> buttonService.getEmployeeInfoForUserLanguage_UZ(updatedEmployee);
                            default -> buttonService.getEmployeeInfoForUserLanguage_RU(updatedEmployee);
                        };
                    }

                    final var messageCompletableFuture = buttonService.askSectionForUpdatingEmployee(update);
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
}