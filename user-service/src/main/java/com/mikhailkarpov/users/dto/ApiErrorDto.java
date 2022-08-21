package com.mikhailkarpov.users.dto;

import lombok.*;
import lombok.experimental.NonFinal;
import org.springframework.http.HttpStatus;

@Value
@NonFinal
public class ApiErrorDto {

    private final int status;
    private final String message;

    public ApiErrorDto(@NonNull HttpStatus status, @NonNull String message) {
        this.status = status.value();
        this.message = message;
    }
}
