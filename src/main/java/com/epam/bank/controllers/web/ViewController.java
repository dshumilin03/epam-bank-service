package com.epam.bank.controllers.web;

import com.epam.bank.dtos.*;
import com.epam.bank.entities.CardStatus;
import com.epam.bank.entities.ChargeStrategyType;
import com.epam.bank.entities.TransactionStatus;
import com.epam.bank.entities.TransactionType;
import com.epam.bank.exceptions.InsufficientFundsException;
import com.epam.bank.exceptions.NotFoundException;
import com.epam.bank.exceptions.UserExistsException;
import com.epam.bank.security.EncryptionService;
import com.epam.bank.security.JwtService;
import com.epam.bank.security.UserDetailsServiceImpl;
import com.epam.bank.services.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@Log4j2
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

    @GetMapping("/service-unavailable")
    public String error() {
        return "service-unavailable";
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

            Boolean currentStatus = userService.getById(userId).getIsDisabled();

            userService.setStatus(userId, !currentStatus);

            redirectAttributes.addFlashAttribute("toggleSuccess",
                    "User " + userId + " successfully " + (!currentStatus ? "DISABLED" : "ACTIVATED"));

            log.info("successfully changed user status");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("toggleError", "Error toggling status: " + e.getMessage());
            log.warn(e);
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
        log.info("successfully opened loan");
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
        log.info("successfully logged out");
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
            log.info("charge applied");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("chargeError", "Charge failed: " + e.getMessage());
            log.warn(e);
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
            HttpServletResponse response, Model model
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
            log.info("successful authentication");
        } catch (BadCredentialsException e) {
            model.addAttribute("loginError", e.getMessage());
            log.warn(e);
            return "login";
        } catch (DisabledException e) {
            model.addAttribute("loginError", "Your account has been disabled. Please contact support.");
            log.warn("user disabled, access denied");
            return "login";
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
        log.warn(ex);
        mav.setViewName("redirect:/");

        return mav;
    }

    @PostMapping("/open-card")
    public String openCard(Model model) {
        try {
            UUID userId = (UUID) model.getAttribute("userId");
            cardService.create(userId, bankAccountService.getByUserId(userId).bankAccountNumber());
            log.info("Successfully created card");
        } catch (Exception e) {
            log.warn(e);
        }

        return "redirect:/dashboard";
    }

    @PostMapping("/register")
    public String register(
            @Valid RegisterRequest registerRequest,
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
            log.info("successfully registered");
            return "redirect:/dashboard";

        } catch (UserExistsException e) {

            model.addAttribute("registrationError", e.getMessage());
            log.warn(e);
            return "register";
        }


    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        UUID userId = (UUID) model.getAttribute("userId");

        try {
            BankAccountDTO bankAccount = bankAccountService.getByUserId(userId);
            List<CardDTO> cards = cardService.getByUserId(userId);
            model.addAttribute("userLoans", loanService.getUserLoansByUserId(userId));
            model.addAttribute("charges", bankAccountService.getChargesByUserId(userId));
            model.addAttribute("userCards", cards);
            model.addAttribute("balance", bankAccount.moneyAmount());

            model.addAttribute("bankAccount", bankAccount);

        } catch (NotFoundException e) {
            model.addAttribute("errorDashboard", e);
        }

        return "dashboard";
    }

    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/manager")
    public String managerDashboard(Model model,
                                   @RequestParam(name = "full_name", required = false) String fullName,
                                   @RequestParam(name = "selected_user_id", required = false) UUID selectedUserId) {

        model.addAttribute("param_fullName", fullName);

        if (fullName != null && !fullName.trim().isEmpty()) {
            try {
                List<UserDTO> foundUsers = userService.getByFullName(fullName.trim());

                foundUsers.forEach(userDTO -> {
                    userDTO.setPassportId(encryptionService.decrypt(userDTO.getPassportId()));
                });

                model.addAttribute("foundUsersList", foundUsers);

                UserDTO selectedUser = null;

                if (selectedUserId != null) {
                    UUID finalSelectedUserId = selectedUserId;
                    selectedUser = foundUsers.stream()
                            .filter(u -> u.getId().equals(finalSelectedUserId))
                            .findFirst()
                            .orElse(null);
                } else if (!foundUsers.isEmpty()) {
                    selectedUser = foundUsers.get(0);
                    selectedUserId = selectedUser.getId();
                }

                if (selectedUser != null) {

                    model.addAttribute("foundUser", selectedUser);
                    model.addAttribute("selectedUserId", selectedUserId);

                    Long accountNumber = selectedUser.getBankAccount().bankAccountNumber();
                    model.addAttribute("accountNumber", accountNumber);

                    model.addAttribute("userLoans", loanService.getUserLoansByUserId(selectedUser.getId()));
                    model.addAttribute("userOutgoingTransactions", bankAccountService.getTransactions(accountNumber, true));
                    model.addAttribute("userIncomingTransactions", bankAccountService.getTransactions(accountNumber, false));

                    model.addAttribute("searchError", null);
                }

                log.info("Entered manager section, users found: {}", foundUsers.size());

            } catch (NotFoundException e) {
                model.addAttribute("foundUsersList", null);
                model.addAttribute("foundUser", null);
                model.addAttribute("searchError", "User: '" + fullName.trim() + "' not found");
                log.warn("Search failed for fullName: {}", fullName.trim(), e);
            }
        }

        return "manager";
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping("/manager")
    public String rollback(String transactionId, Model model) {

        UUID idToRefund;

        try {
            idToRefund = UUID.fromString(transactionId);
        } catch (IllegalArgumentException e) {
            log.warn(e);
            model.addAttribute("rollbackError", "Refund error: Invalid format. Transaction ID must be a valid UUID");

            return "manager";
        }

        try {
            transactionService.refund(idToRefund);
            model.addAttribute("rollbackSuccess", true);
            log.info("Rollback success");

        } catch (NotFoundException | IllegalArgumentException e) {
            log.warn(e);
            model.addAttribute("rollbackError", "Refund error : " + e.getMessage());

        } catch (Exception e) {
            log.warn(e);
            model.addAttribute("rollbackError", "Unknown error: " + e.getMessage());
        }

        return "manager";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/charge-payment")
    public String chargePayment(Model model) {
        UUID userId = (UUID) model.getAttribute("userId");
        try {
            List<CardDTO> cards = cardService.getByUserId(userId).stream()
                    .filter((cardDTO -> cardDTO.getStatus().equals(CardStatus.ACTIVE))).toList();
            model.addAttribute("userCards", cards);
            model.addAttribute("userCharges", bankAccountService.getChargesByUserId(userId));
            model.addAttribute("bankAccount", bankAccountService.getByUserId(userId));
            log.info("Successfully added attributes for charge payment");
        } catch (Exception e) {
            log.warn(e);
        }
        return "charge-payment";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/loan-payment/process")
    public String processLoanPayment(
            String transactionId, Model model
    ) {
        try {
            transactionService.processTransaction(UUID.fromString(transactionId));
            model.addAttribute("success", "Payment successful!");
            log.info("payment success");
        } catch (InsufficientFundsException e) {
            model.addAttribute("Payment failed: Insufficient funds");
            log.warn(e);
        } catch (Exception e) {
            log.warn(e);
            model.addAttribute("Payment failed: " + e.getMessage());
        }

        return "redirect:/dashboard";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/transfer")
    public String transfer(Model model) {
        UUID userId = (UUID) model.getAttribute("userId");
        try {
            model.addAttribute("userCards", cardService.getByUserId(userId));
            model.addAttribute("bankAccount", bankAccountService.getByUserId(userId));
            log.info("Successfully added attributes for transfer");
        } catch (Exception e) {
            log.warn(e);
            model.addAttribute("transferError", e);
        }
        return "transfer";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/card/renew")
    public String renewCardWeb(@RequestParam UUID cardId, RedirectAttributes redirectAttributes) {
        try {
            cardService.renew(cardId);

            redirectAttributes.addFlashAttribute("success", "Card successfully renewed!");
            log.info("Card {} successfully renewed.", cardId);

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to renew card: " + e.getMessage());
            log.warn("Failed to renew card {}: {}", cardId, e.getMessage());
        }

        return "redirect:/dashboard";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/transfer/process")
    public String processTransfer(
            Long targetBankAccount,
            BigDecimal moneyAmount,
            RedirectAttributes redirectAttributes, Model model
    ) {
        UUID transactionId = null;
        try {
            UUID userId = (UUID) model.getAttribute("userId");
            TransactionRequestDTO transactionRequestDTO =
                    new TransactionRequestDTO(moneyAmount, "transfer", TransactionType.TRANSFER, bankAccountService.getByUserId(userId).bankAccountNumber(), targetBankAccount);
            transactionId = transactionService.create(transactionRequestDTO).getId();
            transactionService.processTransaction(transactionId);
            log.info("Transfer successful");

            redirectAttributes.addFlashAttribute("success", "Transfer successful!");
        } catch (InsufficientFundsException e) {
            log.warn(e);
            if (transactionId != null) {
                TransactionDTO transaction = transactionService.getById(transactionId);
                transaction.setStatus(TransactionStatus.FAILED);
                transactionService.update(transaction);
            }
            redirectAttributes.addFlashAttribute("transferError", "Transfer failed: Insufficient funds on account.");
        } catch (Exception e) {
            log.warn(e);
            if (transactionId != null) {
                TransactionDTO transaction = transactionService.getById(transactionId);
                transaction.setStatus(TransactionStatus.FAILED);
                transactionService.update(transaction);
            }
            redirectAttributes.addFlashAttribute("transferError", "Payment failed: " + e.getMessage());
            return "redirect:/transfer";
        }

        return "redirect:/dashboard";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/bank-account")
    public String openBankAccountSubmit(Model model) {
        try {
            UUID userId = (UUID) model.getAttribute("userId");

            bankAccountService.create(userId);
            log.info("Successfully created bank account");
        } catch (Exception e) {
            log.warn(e);
        }
        return "redirect:/dashboard";
    }

    @RequestMapping("/**")
    public String handleUnknownRequest() {

        log.warn("Unknown request received. Redirecting to not found page");

        return "not-found";
    }
}
