package com.epam.bank.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpClientErrorException.Forbidden.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleForbidden(HttpClientErrorException.Forbidden ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "error";
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFoundException(NotFoundException ex, Model model) {

        model.addAttribute("errorMessage", ex.getMessage());
        return "404";
    }

    @ExceptionHandler(ResponseStatusException.class)
    public String handleResponseStatusException(ResponseStatusException ex, Model model) {
        model.addAttribute("status", ex.getStatusCode().value());
        model.addAttribute("errorMessage", ex.getReason());

        if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
            return "404";
        }
        return "error";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleOtherExceptions(Exception ex, Model model) {
        ex.printStackTrace();
        model.addAttribute("errorMessage", "Произошла внутренняя ошибка сервера: " + ex.getMessage());
        return "error";
    }

}
