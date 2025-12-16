package com.epam.bank.controllers.web;

import com.epam.bank.dtos.*;
import com.epam.bank.entities.ChargeStrategyType;
import com.epam.bank.entities.TransactionType;
import com.epam.bank.exceptions.ExistsException;
import com.epam.bank.exceptions.NotFoundException;
import com.epam.bank.security.EncryptionService;
import com.epam.bank.security.JwtService;
import com.epam.bank.security.UserDetailsServiceImpl;
import com.epam.bank.services.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
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
    private final SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
    private final EncryptionService encryptionService;
    private final ChargeService chargeService;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/loan")
    public String loan() {
        return "open-loan";
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping("/toggle-disable-user")
    public String toggleDisableUser(
            @RequestParam("userId") UUID userId,
            @RequestParam(value = "fullName", required = false) String fullName,
            RedirectAttributes redirectAttributes
    ) {
        try {

            boolean currentStatus = userService.getById(userId).getIsDisabled();

            userService.setStatus(userId, !currentStatus);

            redirectAttributes.addFlashAttribute("toggleSuccess",
                    "User " + userId + " successfully " + (!currentStatus ? "DISABLED" : "ACTIVATED"));

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("toggleError", "Error toggling status: " + e.getMessage());
        }

        if (fullName != null && !fullName.trim().isEmpty()) {
            redirectAttributes.addAttribute("full_name", fullName);
        }

        return "redirect:/manager";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/loan")
    public String openLoan(Model model, BigDecimal moneyLeft, ChargeStrategyType chargeStrategyType, Long termMonths) {
        UUID userId = (UUID) model.getAttribute("userId");

        Long bankAccountNumber = userService.getById(userId).getBankAccount().bankAccountNumber();
        Random random = new Random();
        Double percent = random.nextDouble(10) + 1;
        LoanRequestDTO loanRequestDTO = new LoanRequestDTO(moneyLeft, percent, chargeStrategyType, bankAccountNumber, termMonths);
        loanService.open(loanRequestDTO);
        return "redirect:/dashboard";
    }

    @GetMapping("/about-us")
    public String aboutUs() {
        return "index";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            logoutHandler.logout(request, response, authentication);
        }

        return "redirect:/";
    }


    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping("/manager/user")
    public String applyCharge(
            @RequestParam("loanId") UUID loanId,
            @RequestParam(value = "fullName", required = false) String fullName,
            RedirectAttributes redirectAttributes
    ) {
        try {
            chargeService.applyCharge(loanService.getEntityById(loanId));

            redirectAttributes.addFlashAttribute("chargeSuccess", true);

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("chargeError", "Charge failed: " + e.getMessage());
        }

        if (fullName != null && !fullName.trim().isEmpty()) {
            redirectAttributes.addAttribute("full_name", fullName);
        }

        return "redirect:/manager";
    }

    @PostMapping("/login")
    public String login(
            String userName,
            String password,
            HttpServletResponse response, Model model, RedirectAttributes redirectAttributes
    ) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userName, password)
            );

            SecurityContextHolder.getContext().setAuthentication(auth);

            UserDetails userDetails = userDetailsService.loadUserByUsername(userName);
            String jwtToken = jwtService.generateToken(userDetails);

            Cookie cookie = new Cookie("JWT", jwtToken);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            response.addCookie(cookie);

        } catch (BadCredentialsException e) {
            model.addAttribute("loginError", e.getMessage());
            return "login";
        } catch (DisabledException e) {
            redirectAttributes.addFlashAttribute("loginError", "Your account has been disabled. Please contact support.");
            return "redirect:/login";
        }

        return "redirect:/dashboard";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ModelAndView handleAuthorizationDeniedException(AuthorizationDeniedException ex) {

        ModelAndView mav = new ModelAndView();

        mav.setViewName("redirect:/");

        return mav;
    }

    @PostMapping("/open-card")
    public String openCard(Model model) {
        UUID userId = (UUID) model.getAttribute("userId");
        cardService.create(userId, bankAccountService.getByUserId(userId).bankAccountNumber());

        return "redirect:/dashboard";
    }

    @PostMapping("/register")
    public String register(
            RegisterRequest registerRequest,
            HttpServletResponse response, Model model
    ) {

        try {
            UserDTO created = userService.register(registerRequest);
            bankAccountService.create(created.getId());
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(created.getEmail(), registerRequest.password())
            );

            SecurityContextHolder.getContext().setAuthentication(auth);

            UserDetails userDetails = userDetailsService.loadUserByUsername(created.getEmail());
            String jwtToken = jwtService.generateToken(userDetails);

            Cookie cookie = new Cookie("JWT", jwtToken);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            response.addCookie(cookie);

            return "redirect:/dashboard";

        } catch (ExistsException e) {

            model.addAttribute("registrationError", e.getMessage());

            return "register";
        }


    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        UUID userId = (UUID) model.getAttribute("userId");

        BankAccountDTO bankAccount = bankAccountService.getByUserId(userId);
        List<CardDTO> cards = cardService.getByUserId(userId);
        model.addAttribute("userLoans", loanService.getUserLoansByUserId(userId));
        model.addAttribute("charges", bankAccountService.getChargesByUserId(userId));
        model.addAttribute("userCards", cards);
        model.addAttribute("balance", bankAccount.moneyAmount());

        model.addAttribute("bankAccount", bankAccount);

        return "dashboard";
    }

    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/manager")
    public String managerDashboard(Model model, @RequestParam(name = "full_name", required = false) String fullName) {

        if (fullName != null && !fullName.trim().isEmpty()) {
            try {
                UserDTO foundUser = userService.getByFullName(fullName.trim());
                foundUser.setPassportId(encryptionService.decrypt(foundUser.getPassportId()));
                model.addAttribute("foundUser", foundUser);
                Long accountNumber = foundUser.getBankAccount().bankAccountNumber();
                model.addAttribute("accountNumber", accountNumber);
                model.addAttribute("userLoans", loanService.getUserLoansByUserId(foundUser.getId()));
                model.addAttribute("userOutgoingTransactions", bankAccountService.getTransactions(accountNumber, true));
                model.addAttribute("userIncomingTransactions", bankAccountService.getTransactions(accountNumber, false));

                model.addAttribute("searchError", null);
            } catch (NotFoundException e) {

                model.addAttribute("foundUser", null);
                model.addAttribute("searchError", "User: '" + fullName.trim() + "' not found");
            }
        }

        return "manager";
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping("/manager")
    public String rollback(String transactionId, RedirectAttributes redirectAttributes) {

        UUID idToRefund;

        try {
            idToRefund = UUID.fromString(transactionId);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("rollbackError",
                    "Refund error: Invalid format. Transaction ID must be a valid UUID.");

            return "redirect:/manager";
        }

        try {
            transactionService.refund(idToRefund);
            redirectAttributes.addFlashAttribute("rollbackSuccess", true);

        } catch (NotFoundException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("rollbackError", "Refund error : " + e.getMessage());

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("rollbackError", "Unknown error: " + e.getMessage());
        }

        return "redirect:/manager";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/charge-payment")
    public String chargePayment(Model model) {
        UUID userId = (UUID) model.getAttribute("userId");
        model.addAttribute("userCards", cardService.getByUserId(userId));
        model.addAttribute("userCharges", bankAccountService.getChargesByUserId(userId));
        model.addAttribute("bankAccount", bankAccountService.getByUserId(userId));
        return "charge-payment";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/loan-payment/process")
    public String processLoanPayment(
            String transactionId,
            Model model
    ) {
        try {
            transactionService.processTransaction(UUID.fromString(transactionId));
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
        model.addAttribute("bankAccount", bankAccountService.getByUserId(userId));
        return "transfer";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/transfer/process")
    public String processTransfer(
            Long targetBankAccount,
            BigDecimal moneyAmount,
            Model model
    ) {
        try {
            UUID userId = (UUID) model.getAttribute("userId");
            TransactionRequestDTO transactionRequestDTO =
                    new TransactionRequestDTO(moneyAmount, "transfer", TransactionType.TRANSFER, bankAccountService.getByUserId(userId).bankAccountNumber(), targetBankAccount);
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
