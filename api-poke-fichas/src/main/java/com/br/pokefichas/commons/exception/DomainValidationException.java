package com.br.pokefichas.commons.exception;

import java.util.List;

public class DomainValidationException extends RuntimeException {

    private final List<String> errors;

    public DomainValidationException(final List<String> errors) {
        super("Falha ao realizar validação: " + String.join(", ", errors));
        this.errors = List.copyOf(errors);
    }

    public List<String> getErrors() {
        return errors;
    }
}
