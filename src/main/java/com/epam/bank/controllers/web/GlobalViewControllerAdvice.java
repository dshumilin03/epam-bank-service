package com.epam.bank.controllers.web;

import com.epam.bank.dtos.UserDTO;
import com.epam.bank.entities.Role;
import com.epam.bank.services.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

@ControllerAdvice
@AllArgsConstructor
@Log4j2
public class GlobalViewControllerAdvice {

    private final UserService userService;

    private static final String ANONYMOUS_USER = "anonymousUser";

    @ExceptionHandler(UsernameNotFoundException.class)
    public ModelAndView handleUserNotFoundException(UsernameNotFoundException ex) {

        ModelAndView mav = new ModelAndView();
        log.warn(ex);
        mav.setViewName("redirect:/");

        return mav;
    }

    @ModelAttribute("userFullName")
    public String getCurrentUserFullName() {
        UserDTO user = getUserByAuthEmail();
        return user != null ? user.getFullName() : "Guest";
    }


    @ModelAttribute("userId")
    public UUID getCurrentUserId() {
        UserDTO user = getUserByAuthEmail();
        return user != null ? user.getId() : null;
    }

    @ModelAttribute("role")
    public Role getCurrentUserRole() {
        UserDTO user = getUserByAuthEmail();
        return user != null ? user.getRole() : null;
    }

    private UserDTO getUserByAuthEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails details) {
            String email = details.getUsername();
            if (email.equals(ANONYMOUS_USER)) {
                return null;
            }
            return userService.getByEmail(email);
        }

        if (principal instanceof String && principal.equals(ANONYMOUS_USER)) {
            return null;
        }

        return null;
    }
}