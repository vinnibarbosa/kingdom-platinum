package com.br.pokefichas.infra.web;

import com.br.pokefichas.commons.dto.ErrorResponse;
import com.br.pokefichas.commons.exception.*;
import com.br.pokefichas.commons.representation.exception.RepresentationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(
            final EntityNotFoundException ex,
            final HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErrorResponse.builder()
                        .status(HttpStatus.NOT_FOUND.value())
                        .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                        .message(ex.getMessage())
                        .path(request.getRequestURI())
                        .build()
        );
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            final BusinessException ex,
            final HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(
                ErrorResponse.builder()
                        .status(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .error(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase())
                        .message(ex.getMessage())
                        .code(ex.getCode())
                        .path(request.getRequestURI())
                        .build()
        );
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            final ValidationException ex,
            final HttpServletRequest request) {
        return ResponseEntity.badRequest().body(
                ErrorResponse.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                        .message(ex.getMessage())
                        .fieldErrors(ex.hasFieldErrors() ? ex.getFieldErrors() : null)
                        .path(request.getRequestURI())
                        .build()
        );
    }

    @ExceptionHandler(DomainValidationException.class)
    public ResponseEntity<ErrorResponse> handleDomainValidationException(
            final DomainValidationException ex,
            final HttpServletRequest request) {
        return ResponseEntity.badRequest().body(
                ErrorResponse.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                        .message("Validação de domínio falhou")
                        .errors(ex.getErrors())
                        .path(request.getRequestURI())
                        .build()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            final MethodArgumentNotValidException ex,
            final HttpServletRequest request) {
        final Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            final String fieldName = ((FieldError) error).getField();
            final String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        return ResponseEntity.badRequest().body(
                ErrorResponse.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                        .message("Erro de validação nos campos informados")
                        .fieldErrors(fieldErrors)
                        .path(request.getRequestURI())
                        .build()
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            final ConstraintViolationException ex,
            final HttpServletRequest request) {
        final Map<String, String> fieldErrors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        ConstraintViolation::getMessage,
                        (existing, replacement) -> existing
                ));

        return ResponseEntity.badRequest().body(
                ErrorResponse.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                        .message("Erro de validação de restrições")
                        .fieldErrors(fieldErrors)
                        .path(request.getRequestURI())
                        .build()
        );
    }

    @ExceptionHandler(com.br.pokefichas.commons.exception.AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            final com.br.pokefichas.commons.exception.AuthenticationException ex,
            final HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ErrorResponse.builder()
                        .status(HttpStatus.UNAUTHORIZED.value())
                        .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                        .message(ex.getMessage())
                        .code(ex.getCode())
                        .path(request.getRequestURI())
                        .build()
        );
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleSpringAuthenticationException(
            final AuthenticationException ex,
            final HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ErrorResponse.builder()
                        .status(HttpStatus.UNAUTHORIZED.value())
                        .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                        .message("Falha na autenticação")
                        .path(request.getRequestURI())
                        .build()
        );
    }

    @ExceptionHandler({UnauthorizedException.class, AccessDeniedException.class})
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(
            final Exception ex,
            final HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                ErrorResponse.builder()
                        .status(HttpStatus.FORBIDDEN.value())
                        .error(HttpStatus.FORBIDDEN.getReasonPhrase())
                        .message("Acesso negado")
                        .path(request.getRequestURI())
                        .build()
        );
    }

    @ExceptionHandler(IntegracaoException.class)
    public ResponseEntity<ErrorResponse> handleIntegracaoException(
            final IntegracaoException ex,
            final HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(
                ErrorResponse.builder()
                        .status(HttpStatus.BAD_GATEWAY.value())
                        .error(HttpStatus.BAD_GATEWAY.getReasonPhrase())
                        .message(ex.getMessage())
                        .code(ex.getCode())
                        .path(request.getRequestURI())
                        .build()
        );
    }

    @ExceptionHandler({OptimisticLockingFailureException.class, jakarta.persistence.OptimisticLockException.class})
    public ResponseEntity<ErrorResponse> handleOptimisticLockException(
            final Exception ex,
            final HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ErrorResponse.builder()
                        .status(HttpStatus.CONFLICT.value())
                        .error(HttpStatus.CONFLICT.getReasonPhrase())
                        .message("O registro foi alterado por outra operacao. Atualize os dados e tente novamente.")
                        .path(request.getRequestURI())
                        .build()
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(
            final MethodArgumentTypeMismatchException ex,
            final HttpServletRequest request) {
        final String message = String.format(
                "Parâmetro '%s' com valor '%s' não pôde ser convertido para o tipo %s",
                ex.getName(),
                ex.getValue(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "desconhecido"
        );

        return ResponseEntity.badRequest().body(
                ErrorResponse.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                        .message(message)
                        .path(request.getRequestURI())
                        .build()
        );
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(
            final NoResourceFoundException ex,
            final HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErrorResponse.builder()
                        .status(HttpStatus.NOT_FOUND.value())
                        .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                        .message("Recurso não encontrado")
                        .path(request.getRequestURI())
                        .build()
        );
    }

    @ExceptionHandler(RepresentationException.class)
    public ResponseEntity<ErrorResponse> handleRepresentationException(
            final RepresentationException ex,
            final HttpServletRequest request) {
        return ResponseEntity.badRequest().body(
                ErrorResponse.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                        .message(ex.getMessage())
                        .path(request.getRequestURI())
                        .build()
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            final IllegalArgumentException ex,
            final HttpServletRequest request) {
        return ResponseEntity.badRequest().body(
                ErrorResponse.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                        .message(ex.getMessage())
                        .path(request.getRequestURI())
                        .build()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            final Exception ex,
            final HttpServletRequest request) {
        log.error("Erro inesperado: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ErrorResponse.builder()
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                        .message("Ocorreu um erro inesperado. Por favor, tente novamente mais tarde.")
                        .path(request.getRequestURI())
                        .build()
        );
    }
}
