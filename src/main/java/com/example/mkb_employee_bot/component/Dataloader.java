package com.example.mkb_employee_bot.component;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.example.mkb_employee_bot.entiry.*;
import com.example.mkb_employee_bot.entiry.enums.*;
import com.example.mkb_employee_bot.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.boot.CommandLineRunner;

@Component
@RequiredArgsConstructor
public class Dataloader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final EmployeeRepository employeeRepository;
    private final PositionRepository positionRepository;
    private final EducationRepository educationRepository;
    private final ManagementRepository managementRepository;
    private final DepartmentRepository departmentRepository;
    private final AttachmentRepository attachmentRepository;

    @Override
    public void run(String... args) throws Exception {

        final var userList = userRepository.findAll();
        if (userList.isEmpty()) {

            final var it = departmentRepository.save(new Department(1L, "IT"));
            final var riskManagement = departmentRepository.save(new Department(2L, "Risk Management"));

            final var yangiTexnologiyalar = managementRepository.save(new Management(1L, "Yangi texnologiyalar", it));
            final var integratsiyalar = managementRepository.save(new Management(2L, "Integratsiyalar", it));
            final var riskTahlili = managementRepository.save(new Management(3L, "Risk tahlili", riskManagement));

            final var mutaxxasis = positionRepository.save(new Position(1L, "Mutaxxasis", yangiTexnologiyalar));
            final var yetakchiMutaxxasis = positionRepository.save(new Position(2L, "Yetakchi Mutaxxasis", integratsiyalar));
            final var kichikMutaxxasis = positionRepository.save(new Position(3L, "Kichik Mutaxxasis", riskTahlili));

            final var education = Education.builder()
                    .name("TSUE")
                    .type(EduType.BACHELOR)
                    .startedDate("2021-8-3")
                    .endDate("Present")
                    .build();
            final var savedEducation = educationRepository.save(education);

            final var skillList = List.of(
                    Skill.builder()
                            .name("Java")
                            .skillType(SkillType.HARD_SKILL)
                            .build(),
                    Skill.builder()
                            .name("OOP")
                            .skillType(SkillType.HARD_SKILL)
                            .build(),
                    Skill.builder()
                            .name("Problem Solving")
                            .skillType(SkillType.SOFT_SKILL)
                            .build()
            );
            final List<Skill> skills = skillRepository.saveAll(skillList);

            Employee employee = Employee.builder()
                    .fullName("Urokov Sardor")
                    .dateOfBirth("2004-10-05")
                    .phoneNumber("998914525468")
                    .nationality("Uzbek")
                    .age(18)
                    .position(kichikMutaxxasis)
                    .education(savedEducation)
                    .skills(skills)
                    .attachments(
                            Collections.emptySet()
                    )
                    .build();
            employeeRepository.save(employee);

            User user = User.builder()
                    .userChatId(632993372L)
                    .fullName("Сардор")
                    .phoneNumber("998914525468")
                    .userName("Sardor_Shukhratovich")
                    .role(Role.USER)
                    .stage(Stage.START)
                    .language(Language.RU)
                    .createdAt(new Date())
                    .updatedAt(new Date())
                    .build();
            userRepository.save(user);
        }
    }
}