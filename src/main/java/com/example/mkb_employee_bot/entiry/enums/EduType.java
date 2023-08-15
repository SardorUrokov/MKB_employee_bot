package com.example.mkb_employee_bot.entiry.enums;

public enum EduType {
    MAKTAB(0),
    KOLLEJ(1),
    AKADEMIK_LITSEY(1),
    BAKALAVRIAT(2),
    MAGISTRATURA(3),
    PhD(4);

    private int value;

    EduType(int value) {
        this.value = value;
    }

    EduType() {}

    public int getValue() {
        return value;
    }
}