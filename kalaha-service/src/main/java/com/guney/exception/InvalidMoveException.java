package com.guney.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Invalid Move")
public class InvalidMoveException extends Exception {

    private final String message;

    public InvalidMoveException(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
