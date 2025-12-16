package com.epam.bank.controllers.web;

import com.epam.bank.dtos.UserDTO;
import com.epam.bank.entities.Role;
import com.epam.bank.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.UUID;

@ControllerAdvice
@AllArgsConstructor
public class GlobalViewControllerAdvice {

    private final UserService userService;

    @ModelAttribute("currentUserFullName")
    public String getCurrentUserFullName() {
        return getUserByAuthEmail().fullName();
    }


    @ModelAttribute("currentUserId")
    public UUID getCurrentUserId() {
        return getUserByAuthEmail().id();
    }

    @ModelAttribute("currentUserRole")
    public Role getCurrentUserRole() {
        return getUserByAuthEmail().role();
    }

    private UserDTO getUserByAuthEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            UserDetails details = (UserDetails) authentication.getPrincipal();
            String email = details.getUsername();
            return userService.getByEmail(email);
        }

        return null;
    }
}