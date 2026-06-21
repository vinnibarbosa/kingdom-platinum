package com.br.pokefichas.commons.entity;

import java.io.Serializable;

public interface IEntity<I> extends Serializable, Identity<I>, AuditEntity {
    default boolean isNew() {
        return getId() == null;
    }
}
