package com.money.manager.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Subtype {
    FIXED("fixed"), VARIABLE("variable"), ONE_TIME("one_time");

    private final String name;

    public static Subtype getSubTypeByName(String name){
        for(Subtype sub : Subtype.values()){
            if(sub.getName().equalsIgnoreCase(name)){
                return sub;
            }
        }
        throw new EnumConstantNotPresentException(Subtype.class, "subtype not found");
    }
}
