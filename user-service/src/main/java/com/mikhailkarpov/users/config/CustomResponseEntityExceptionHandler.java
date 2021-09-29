package com.mikhailkarpov.users.config;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;
import java.io.IOException;

@RestControllerAdvice
public class CustomResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(WebApplicationException.class)
    public void handleWebApplicationException(HttpServletResponse response,
                                              WebApplicationException e) throws IOException {
        int status = e.getResponse().getStatus();
        response.sendError(status);
    }
}
