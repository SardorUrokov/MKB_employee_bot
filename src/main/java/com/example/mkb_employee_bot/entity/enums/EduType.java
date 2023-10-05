package com.example.mkb_employee_bot.entity.enums;

public enum EduType {
    MAKTAB(0),
    KOLLEJ(1),
    AKADEMIK_LITSEY(1),
    MUTAXXASISLIK_KURSI(4),
    BAKALAVRIAT(2),
    MAGISTRATURA(3);

    private int value;

    EduType(int value) {
        this.value = value;
    }

    EduType() {}

    public int getValue() {
        return value;
    }
}