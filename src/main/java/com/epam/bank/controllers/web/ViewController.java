package com.epam.bank.controllers.web;

import com.epam.bank.dtos.*;
import com.epam.bank.entities.TransactionType;
import com.epam.bank.security.JwtService;
import com.epam.bank.security.UserDetailsServiceImpl;
import com.epam.bank.services.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class ViewController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtService jwtService;
    private final UserService userService;
    private final BankAccountService bankAccountService;
    private final LoanService loanService;
    private final TransactionService transactionService;
    private final CardService cardService;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/about-us")
    public String aboutUs() {
        return "index";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam String email,
            @RequestParam String password,
            HttpServletResponse response
    ) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        SecurityContextHolder.getContext().setAuthentication(auth);

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        String jwtToken = jwtService.generateToken(userDetails);

        Cookie cookie = new Cookie("JWT", jwtToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        response.addCookie(cookie);

        return "redirect:/dashboard";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(
            RegisterRequest registerRequest,
            HttpServletResponse response
    ) {

        UserDTO created = userService.register(registerRequest);
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(created.email(), created.password())
        );

        SecurityContextHolder.getContext().setAuthentication(auth);

        UserDetails userDetails = userDetailsService.loadUserByUsername(created.email());
        String jwtToken = jwtService.generateToken(userDetails);

        Cookie cookie = new Cookie("JWT", jwtToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        response.addCookie(cookie);

        return "redirect:/dashboard";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        UUID userId = (UUID) model.getAttribute("userId");

        List<BankAccountDTO> bankAccounts = bankAccountService.getByUserId(userId);
        List<CardDTO> cards = cardService.getByUserId(userId);
        model.addAttribute("userLoans", loanService.getUserLoansByUserId(userId));

        model.addAttribute("userCards", cards);

        model.addAttribute("bankAccounts", bankAccounts);

        return "dashboard";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/manager")
    public String managerDashboard(Model model, @RequestParam(name = "full_name", required = false) String fullName) {

        if (fullName != null) {
            UserDTO foundUser = userService.getByFullName(fullName);
            model.addAttribute("foundUser", foundUser);
            Long accountNumber = foundUser.bankAccount().bankAccountNumber();
            model.addAttribute("userOutgoingTransactions", bankAccountService.getTransactions(accountNumber, true));
            model.addAttribute("userIncomingTransactions", bankAccountService.getTransactions(accountNumber, false));
        }
        return "manager";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/loan-payment")
    public String loanPayment(Model model) {
        UUID userId = (UUID) model.getAttribute("userId");
        model.addAttribute("userCards", cardService.getByUserId(userId));
        model.addAttribute("userCharges", bankAccountService.getLoans(userId));
        return "loan-payment";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/loan-payment/process")
    public String processLoanPayment(
            UUID transactionId,
            Model model
    ) {
        try {
            transactionService.processTransaction(transactionId);
            model.addAttribute("success", "Payment successful!");
        } catch (Exception e) {
            model.addAttribute("error", "Payment failed: " + e.getMessage());
        }

        return "redirect:/dashboard";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/transfer")
    public String transfer(Model model) {
        UUID userId = (UUID) model.getAttribute("userId");
        model.addAttribute("userCards", cardService.getByUserId(userId));
        return "transfer";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/transfer/process")
    public String processTransfer(
            Long targetBankAccount,
            Long sourceBankAccount,
            BigDecimal moneyAmount,
            Model model
    ) {
        try {
            TransactionRequestDTO transactionRequestDTO =
                    new TransactionRequestDTO(moneyAmount, "transfer", TransactionType.TRANSFER, sourceBankAccount, targetBankAccount);
            transactionService.processTransaction(transactionService.create(transactionRequestDTO).getId());

            model.addAttribute("success", "Transfer successful!");
        } catch (Exception e) {
            model.addAttribute("error", "Payment failed: " + e.getMessage());
        }

        return "redirect:/dashboard";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/bank-account")
    public String openBankAccountSubmit(Model model) {
        UUID userId = (UUID) model.getAttribute("userId");

        bankAccountService.create(userId);

        return "redirect:/dashboard";
    }
}
