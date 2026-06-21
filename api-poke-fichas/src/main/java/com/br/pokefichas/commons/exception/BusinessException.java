package com.br.pokefichas.commons.exception;

public class BusinessException extends RuntimeException {

    private final String code;

    public BusinessException(String message) {
        super(message);
        this.code = "BUSINESS_RULE_VIOLATION";
    }

    public BusinessException(String message, String code) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = "BUSINESS_RULE_VIOLATION";
    }

    public String getCode() {
        return code;
    }
}
