package com.money.manager.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Type {
    INCOME("income"), EXPENSE("expense");

    private String name;

    public static Type getTypeByName(String name) {
        for (Type type : Type.values()) {
            if (type.getName().equalsIgnoreCase(name)) {
                return type;
            }
        }
        throw new EnumConstantNotPresentException(Type.class, "type not found");
    }
}
