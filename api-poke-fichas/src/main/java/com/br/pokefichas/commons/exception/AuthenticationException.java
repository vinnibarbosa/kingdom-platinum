package com.br.pokefichas.commons.exception;

public class AuthenticationException extends RuntimeException {

    private static final String CODE_PREFIX = "AUTH";
    public static final String INVALID_CREDENTIALS = "001";
    public static final String EXPIRED_TOKEN = "002";
    public static final String INVALID_TOKEN = "003";
    public static final String UNAUTHORIZED_ACCESS = "004";

    private final String code;

    private AuthenticationException(String message, String code) {
        super(message);
        this.code = CODE_PREFIX + "_" + code;
    }

    private AuthenticationException(String message, String code, Throwable cause) {
        super(message, cause);
        this.code = CODE_PREFIX + "_" + code;
    }

    public static AuthenticationException invalidCredentials() {
        return new AuthenticationException("Credenciais inválidas", INVALID_CREDENTIALS);
    }

    public static AuthenticationException expiredToken() {
        return new AuthenticationException("Token expirado", EXPIRED_TOKEN);
    }

    public static AuthenticationException invalidToken() {
        return new AuthenticationException("Token inválido", INVALID_TOKEN);
    }

    public static AuthenticationException unauthorizedAccess() {
        return new AuthenticationException("Acesso não autorizado", UNAUTHORIZED_ACCESS);
    }

    public static AuthenticationException custom(String message, String code) {
        return new AuthenticationException(message, code);
    }

    public static AuthenticationException withCause(String message, String code, Throwable cause) {
        return new AuthenticationException(message, code, cause);
    }

    public String getCode() {
        return code;
    }
}
