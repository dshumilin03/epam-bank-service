package com.epam.bank.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.IOException;

@Log4j2
@ControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler({BankServiceException.class})
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public void handleBankServiceException(BankServiceException ex,
                                           HttpServletResponse response,
                                           HttpServletRequest request) throws IOException {

        log.error("Bank Service exception", ex);

        clearSession(request);
        response.sendRedirect("/service-unavailable");
    }

    @ExceptionHandler({BankServiceRuntimeException.class})
    public void handleBankServiceRuntimeException(BankServiceException ex, HttpServletResponse response, HttpServletRequest request) throws IOException {
        log.warn("Bank Service Runtime Exception exception" + ex);
    }


    @ExceptionHandler({DataAccessResourceFailureException.class})
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public void handleDataAccessResourceFailure(DataAccessResourceFailureException ex, HttpServletResponse response, HttpServletRequest request) throws IOException {

        log.error("Database access resource failure: " + ex.getMessage());

        clearSession(request);
        response.sendRedirect("/service-unavailable");

    }


    @ExceptionHandler({InternalAuthenticationServiceException.class})
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public void handleInternalAuthenticationServiceException(InternalAuthenticationServiceException ex, HttpServletResponse response, HttpServletRequest request) throws IOException {

        clearSession(request);
        log.error("Authentication failure, internal services are not responding: ", ex);
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

    private void clearSession(HttpServletRequest request) {
        SecurityContextHolder.clearContext();

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }
}
