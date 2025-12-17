package com.epam.bank.logging;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    @Pointcut("execution(public * com.epam.bank.controllers..*.*(..)) || " +
            "execution(public * com.epam.bank.services..*.*(..)) ||" +
            "execution(public * com.epam.bank.repositories..*.*(..)) ||" +
            "execution(public * com.epam.bank.mappers..*.*(..))")
    public void applicationMethods() {
    }

    @Before("applicationMethods()")
    public void logMethodEntry(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();

        String args = Arrays.stream(joinPoint.getArgs())
                .map(this::formatArgument)
                .collect(Collectors.joining(", "));

        log.info("ENTRY: {}.{}({})", className, methodName, args);
    }

    @AfterReturning(pointcut = "applicationMethods()", returning = "result")
    public void logMethodExit(JoinPoint joinPoint, Object result) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();

        String formattedResult = formatResult(result);

        log.info("EXIT: {}.{}() returned: {}", className, methodName, formattedResult);
    }

    private String formatArgument(Object arg) {
        if (arg == null) {
            return "null";
        }
        String simpleName = arg.getClass().getSimpleName();

        if (simpleName.toLowerCase().contains("password") || simpleName.toLowerCase().contains("jwt") || arg.toString().length() > 255) {
            return simpleName + ": ***REDACTED***";
        }

        if (simpleName.contains("Request") || simpleName.contains("Response") || simpleName.contains("Model") || simpleName.contains("SecurityContext")) {
            return simpleName + " Instance";
        }

        return arg.toString();
    }

    private String formatResult(Object result) {
        if (result == null) {
            return "void/null";
        }
        String simpleName = result.getClass().getSimpleName();

        if (simpleName.toLowerCase().contains("password") || simpleName.toLowerCase().contains("jwt") || simpleName.toLowerCase().contains("token")) {
            return simpleName + ": ***REDACTED_RETURN***";
        }

        if (result instanceof java.util.Collection) {
            return simpleName + " List (Size: " + ((java.util.Collection<?>) result).size() + ")";
        }

        return result.toString();
    }
}