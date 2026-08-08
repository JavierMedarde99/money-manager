package com.money.manager.domain.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class NotFoundException extends Exception{
    public NotFoundException(String menssage){
        super(menssage);
    }
}
