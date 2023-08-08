package com.example.mkb_employee_bot.entiry.enums;

public enum Stage {
    START(1),
    SELECT_LANGUAGE(2),
    SHARE_CONTACT(3),
    CLARIFICATION_ROLE(0),
    SELECT_SECTION(4),
    ENTER_EMPLOYEE_NAME_ROLE_ADMIN(),
    ENTER_EMPLOYEE_BIRTHDATE_ROLE_ADMIN,
    ENTER_EMPLOYEE_PHONE_NUMBER_ROLE_ADMIN,
    ENTER_EMPLOYEE_ADDITIONAL_PHONE_NUMBER_ROLE_ADMIN,
    SELECT_POSITION_DEPARTMENT,
    ENTER_EMPLOYEE_POSITION_ROLE_ADMIN,
    SELECT_EDUCATION_TYPE,
    ENTER_EDUCATION,
    SELECT_FILE_TYPE,
    SHARE_ATTACHMENT,
    ENTER_SKILLS;

    private int value;

    Stage(int value) {
        this.value = value;
    }

    Stage() {}

    public int getValue() {
        return value;
    }
}