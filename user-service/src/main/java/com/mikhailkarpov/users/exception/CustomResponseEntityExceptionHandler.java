package com.mikhailkarpov.users.exception;

import com.mikhailkarpov.users.dto.ApiErrorDto;
import com.mikhailkarpov.users.dto.ApiValidationErrorDto;
import com.mikhailkarpov.users.dto.ApiValidationErrorDto.FieldViolation;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class CustomResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<Object> handleResourceAlreadyExistsException(ResourceAlreadyExistsException e, WebRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return buildResponse(e, request, status);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFoundException(ResourceNotFoundException e, WebRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        return buildResponse(e, request, status);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatus status,
                                                                  WebRequest request) {

        List<FieldViolation> violations = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> new FieldViolation(fieldError.getField(), fieldError.getDefaultMessage()))
                .collect(Collectors.toList());

        ApiValidationErrorDto errorDto =
                new ApiValidationErrorDto(status, "Validation failed for argument(s)", violations);

        return handleExceptionInternal(ex, errorDto, new HttpHeaders(), status, request);
    }

    private ResponseEntity<Object> buildResponse(Exception e, WebRequest request, HttpStatus status) {
        ApiErrorDto apiError = new ApiErrorDto(status, e.getMessage());
        return handleExceptionInternal(e, apiError, new HttpHeaders(), status, request);
    }


}
