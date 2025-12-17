package com.epam.bank.exceptions;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.IOException;

@Log4j2
@ControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler({BankServiceRuntimeException.class})
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public void handleBankServiceRuntimeException(BankServiceRuntimeException ex, HttpServletResponse response) throws IOException {

        log.error("Database access resource failure: ", ex);

        response.sendRedirect("/service-unavailable");
    }

    @ExceptionHandler({InternalAuthenticationServiceException.class})
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public void handleInternalAuthenticationServiceException(InternalAuthenticationServiceException ex, HttpServletResponse response) throws IOException {

        log.error("Database access resource failure: ", ex);

        response.sendRedirect("/service-unavailable");
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFoundException(NotFoundException ex, Model model) {
        log.warn(ex);
        model.addAttribute("errorMessage", ex.getMessage());
        return "not-found";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleOtherExceptions(Exception ex, Model model) {
        log.warn(ex);
        model.addAttribute("errorMessage", "Internal server error: " + ex.getMessage());
        return "error";
    }

}
