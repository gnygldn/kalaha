package com.guney.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Game not found")
public class InvalidGameException extends Exception {

    private final String message;

    public InvalidGameException(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
