package com.br.pokefichas.commons.exception;

public class EntityNotFoundException extends RuntimeException {

    private final String entityType;
    private final Object identifier;

    public EntityNotFoundException(String message) {
        super(message);
        this.entityType = null;
        this.identifier = null;
    }

    public EntityNotFoundException(String entityType, Object identifier) {
        super(String.format("%s com identificador '%s' não foi encontrado(a)", entityType, identifier));
        this.entityType = entityType;
        this.identifier = identifier;
    }

    public String getEntityType() {
        return entityType;
    }

    public Object getIdentifier() {
        return identifier;
    }
}
