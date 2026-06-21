package com.br.pokefichas.commons.exception;

public class IntegracaoException extends RuntimeException {

    private final String code;

    public IntegracaoException(String message) {
        super(message);
        this.code = "INTEGRATION_ERROR";
    }

    public IntegracaoException(String message, String code) {
        super(message);
        this.code = code;
    }

    public IntegracaoException(String message, Throwable cause) {
        super(message, cause);
        this.code = "INTEGRATION_ERROR";
    }

    public String getCode() {
        return code;
    }
}
