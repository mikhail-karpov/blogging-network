package com.mikhailkarpov.users.dto;

import lombok.NonNull;
import lombok.Value;
import org.springframework.http.HttpStatus;

import java.util.List;

@Value
public class ApiValidationErrorDto extends ApiErrorDto {

    List<FieldViolation> violations;

    public ApiValidationErrorDto(@NonNull HttpStatus status, @NonNull String message, @NonNull List<FieldViolation> violations) {
        super(status, message);
        this.violations = violations;
    }

    @Value
    public static class FieldViolation {

        @NonNull String field;
        @NonNull String message;
    }
}
