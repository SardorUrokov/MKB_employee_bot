package com.example.mkb_employee_bot.component;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.example.mkb_employee_bot.entity.*;
import com.example.mkb_employee_bot.entity.enums.*;
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

    @Override
    public void run(String... args) throws Exception {

        final var userList = userRepository.findAll();
        if (userList.isEmpty()) {

            final var it = departmentRepository.save(new Department("IT"));
            final var riskManagement = departmentRepository.save(new Department("Risk Management"));

            final var yangiTexnologiyalar = managementRepository.save(new Management("Yangi texnologiyalar", it));
            final var integratsiyalar = managementRepository.save(new Management("Integratsiyalar", it));
            final var riskTahlili = managementRepository.save(new Management("Risk tahlili", riskManagement));

            final var mutaxxasis = positionRepository.save(new Position("Mutaxxasis", yangiTexnologiyalar));
            final var yetakchiMutaxxasis = positionRepository.save(new Position("Yetakchi Mutaxxasis", integratsiyalar));
            final var kichikMutaxxasis = positionRepository.save(new Position("Kichik Mutaxxasis", riskTahlili));

            final var education = Education.builder()
                    .name("TSUE")
                    .educationField("Soliqlar va soliqqa tortish")
                    .type(EduType.BAKALAVRIAT)
                    .startedDate("2021-08-03")
                    .endDate("Present")
                    .build();
            final var education1 = Education.builder()
                    .name("TUIT")
                    .educationField("Dasturiy Injiniring")
                    .type(EduType.MAGISTRATURA)
                    .startedDate("2026-08-03")
                    .endDate("Present")
                    .build();

            educationRepository.saveAll(List.of(education, education1));

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
                    .position(mutaxxasis)
                    .educations(List.of(education, education1))
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
                    .role(Role.SUPER_ADMIN)
                    .stage(Stage.STARTED)
                    .language(Language.RU)
                    .createdAt(new Date())
                    .updatedAt(new Date())
                    .build();

            User user1 = User.builder()
                    .userChatId(612492175L)
                    .fullName("Сардор")
                    .phoneNumber("998981234567")
                    .userName("Sardor_Sh")
                    .role(Role.ADMIN)
                    .stage(Stage.STARTED)
                    .language(Language.UZ)
                    .createdAt(new Date())
                    .updatedAt(new Date())
                    .build();

            userRepository.saveAll(List.of(user, user1));
        }
    }
}