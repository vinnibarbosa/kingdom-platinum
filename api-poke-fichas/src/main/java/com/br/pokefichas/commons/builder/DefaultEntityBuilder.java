package com.br.pokefichas.commons.builder;

import com.br.pokefichas.commons.entity.BaseEntity;

import java.util.Objects;

public abstract class DefaultEntityBuilder<T> implements IBuilder<T> {

    protected T entity;
    protected final T originalEntity;
    protected final EntityState state;
    protected boolean managed = false;

    protected DefaultEntityBuilder(final T entity, final EntityState state) {
        Objects.requireNonNull(entity, "Entity cannot be null");
        Objects.requireNonNull(state, "State cannot be null");

        this.state = state;

        if (state == EntityState.NEW) {
            this.entity = entity;
            this.originalEntity = null;
            return;
        }

        this.originalEntity = entity;
        this.entity = entity;
    }

    @SuppressWarnings("unchecked")
    public <B extends DefaultEntityBuilder<T>> B managed(final boolean managed) {
        this.managed = managed;
        return (B) this;
    }

    protected void beforeValidate() {
    }

    protected void afterValidate() {
    }

    protected void validateBusiness() {
    }

    @Override
    public final T build() {
        return build(true);
    }

    public final T build(final boolean validateBusiness) {
        Objects.requireNonNull(entity, "Entity cannot be null");
        beforeValidate();
        if (validateBusiness) {
            invokeBeforeLifecycle();
            validateBusiness();
            invokeAfterLifecycle();
        }
        afterValidate();
        return buildEntity();
    }

    private void invokeBeforeLifecycle() {
        if (!(entity instanceof BaseEntity<?> baseEntity)) {
            return;
        }
        if (baseEntity.isNew()) {
            baseEntity.invokeBeforeCreate();
            return;
        }
        baseEntity.invokeBeforeUpdate();
    }

    private void invokeAfterLifecycle() {
        if (!(entity instanceof BaseEntity<?> baseEntity)) {
            return;
        }
        if (baseEntity.isNew()) {
            baseEntity.invokeAfterCreate();
            return;
        }
        baseEntity.invokeAfterUpdate();
    }

    private T buildEntity() {
        try {
            return entity;
        } finally {
            entity = null;
        }
    }

    protected enum EntityState {
        NEW,
        BUILT
    }
}
