package com.catanio.ecommerce.api.exception;

import com.catanio.ecommerce.domain.exception.BusinessException;
import com.catanio.ecommerce.domain.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 404 — recurso não encontrado
    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFound(ResourceNotFoundException ex) {
        log.debug("Resource not found: {}", ex.getMessage());
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setType(URI.create("/errors/resource-not-found"));
        problem.setTitle("Resource Not Found");
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    // 409 — violação de regra de negócio
    @ExceptionHandler(BusinessException.class)
    public ProblemDetail handleBusinessException(BusinessException ex) {
        log.debug("Business rule violation: {}", ex.getMessage());
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setType(URI.create("/errors/business-rule-violation"));
        problem.setTitle("Business Rule Violation");
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    // 400 — falha de validação (Bean Validation)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "Invalid value",
                (first, second) -> first
            ));

        log.debug("Validation failed: {}", errors);
        var problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST, "Request validation failed"
        );
        problem.setType(URI.create("/errors/validation-failed"));
        problem.setTitle("Validation Failed");
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("fields", errors);
        return problem;
    }

    // 400 — domain IllegalArgumentException (ex: stock negativo)
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        log.debug("Illegal argument: {}", ex.getMessage());
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setType(URI.create("/errors/invalid-argument"));
        problem.setTitle("Invalid Argument");
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    // 500 — fallback para erros inesperados
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        var problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred"
        );
        problem.setType(URI.create("/errors/internal-server-error"));
        problem.setTitle("Internal Server Error");
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }
}
