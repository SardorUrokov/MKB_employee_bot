package com.example.mkb_employee_bot.entity.enums;

import jakarta.persistence.AssociationOverride;

public enum Stage {
    STARTED(),
    LANGUAGE_SELECTED(),
    CONTACT_SHARED(),
    CONTACT_SET(),
    ROLE_IDENTIFIED(),
    SECTION_SELECTED(),
    DEPARTMENT_SELECTED_FOR_EMPLOYEE_INFO(),
    DEPARTMENT_SELECTED_FOR_DELETING(),
    DEPARTMENT_SELECTED_FOR_UPDATING(),
    DEPARTMENT_SELECTED_FOR_CREATING_MANAGEMENT(),
    ENTER_NAME_FOR_UPDATE_DEPARTMENT(),
    ENTER_NAME_FOR_CREATE_DEPARTMENT(),

    ENTER_NAME_FOR_CREATE_MANAGEMENT(),
    MANAGEMENT_SELECTED_FOR_DELETING(),
    MANAGEMENT_SELECTED_FOR_UPDATING(),
    DEPARTMENT_SELECTED_FOR_UPDATING_MANAGEMENT(),
    DEPARTMENT_SELECTED_FOR_SAVING_UPDATED_MANAGEMENT(),
    ENTER_NAME_FOR_SAVING_UPDATED_MANAGEMENT(),

    POSITION_SELECTED_FOR_DELETING(),
    POSITION_SELECTED_FOR_UPDATING(),
    MANAGEMENT_SELECTED_FOR_CREATING_POSITION(),
    MANAGEMENT_SELECTED_FOR_UPDATING_POSITION(),
    ENTER_NAME_FOR_CREATING_POSITION_NAME(),
    ENTER_NAME_FOR_UPDATE_POSITION(),
    POSITION_FOR_CREATING_EMPLOYEE(),
    ENTERED_POSITION_NAME_FOR_CREATING_EMPLOYEE(),

    ADMIN_SELECTED_FOR_DELETING(),
    ENTER_PHONE_NUMBER_FOR_CREATING_ADMIN(),
    ADMIN_CREATED(),

    CONFIRMATION_FOR_DELETING_EMPLOYEE(),
    MANAGEMENT_SELECTED_FOR_CREATING_EMPLOYEE(),
    MANAGEMENT_SELECTED_FOR_EMPLOYEE_INFO(),
    POSITION_SELECTED_FOR_EMPLOYEE_INFO(),
    DEPARTMENT_EMPLOYEE_SELECTED(),
    ENTERED_EMPLOYEE_NAME_FOR_SEARCH_ROLE_USER(),
    ENTERED_EMPLOYEE_NAME_FOR_DELETE_ROLE_USER(),
    ENTERED_EMPLOYEE_NAME_FOR_UPDATING_ROLE_ADMIN(),
    SELECTED_EMPLOYEE_NAME_FOR_SEARCH_ROLE_USER(),
    SELECTED_EMPLOYEE_NAME_FOR_DELETING_ROLE_ADMIN(),
    SELECTED_EMPLOYEE_NAME_FOR_UPDATING_ROLE_ADMIN(),
    SELECTED_EMPLOYEE_UPDATING_INFO_ROLE_ADMIN(),

    ENTERED_EMPLOYEE_NAME_ROLE_ADMIN(0),
    ENTERED_EMPLOYEE_PHONE_NUMBER_ROLE_ADMIN(1),
    ENTERED_EMPLOYEE_BIRTHDATE_ROLE_ADMIN(2),
    ENTERED_EMPLOYEE_NATIONALITY(3),
    SELECTED_EMPLOYEE_EDUCATION_TYPE(4),
    ENTERED_EMPLOYEE_EDUCATION_NAME(5),
    ENTERED_EMPLOYEE_EDUCATION_FIELD(6),
    ENTERED_EMPLOYEE_EDUCATION_PERIOD(7),
    ENTERED_EMPLOYEE_SKILLS(8),
    SELECTED_EMPLOYEE_FILE_TYPE(9),
    ATTACHMENT_SHARED(10),
    PROCEDURE_WITH_ATTACHMENTS(),
    EMPLOYEE_CREATED,

    SELECTED_EMPLOYEE_2ND_EDUCATION_TYPE(),
    ENTERED_EMPLOYEE_2ND_EDUCATION_PERIOD(),
    EMPLOYEE_UPDATING_POSITION_SELECTED(),
    MANAGEMENT_SELECTED_FOR_UPDATING_EMPLOYEE_POSITION(),
    SELECTED_EMPLOYEE_UPDATING_EDUCATION(),
    SELECTED_UPDATING_EDUCATION_TYPE(),
    ENTERED_UPDATING_EDUCATION_NAME(),
    ENTERED_UPDATING_EDUCATION_FIELD(),
    ENTERED_UPDATING_EDUCATION_PERIOD();

    private int value;

    Stage(int value) {
        this.value = value;
    }

    Stage() {}

    public int getValue() {
        return value;
    }
}