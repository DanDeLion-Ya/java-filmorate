package ru.yandex.practicum.filmorate.exception;

import lombok.Getter;

@Getter
public class ExceptionResponse {
    private final String message;

    public ExceptionResponse(String message) {
        this.message = message;
    }
}