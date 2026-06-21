package com.br.pokefichas.commons.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity<I> implements IEntity<I> {

    public abstract void setId(I id);

    protected void beforeCreate() {
    }

    protected void afterCreate() {
    }

    protected void beforeUpdate() {
    }

    protected void afterUpdate() {
    }

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private String createdBy;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    @LastModifiedBy
    @Column(name = "updated_by")
    private String updatedBy;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Override
    public Instant getCreatedAt() { return createdAt; }

    @Override
    public void setCreatedAt(final Instant createdAt) { this.createdAt = createdAt; }

    @Override
    public String getCreatedBy() { return createdBy; }

    @Override
    public void setCreatedBy(final String createdBy) { this.createdBy = createdBy; }

    @Override
    public Instant getUpdatedAt() { return updatedAt; }

    @Override
    public void setUpdatedAt(final Instant updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String getUpdatedBy() { return updatedBy; }

    @Override
    public void setUpdatedBy(final String updatedBy) { this.updatedBy = updatedBy; }

    public Long getVersion() { return version; }

    public void setVersion(final Long version) { this.version = version; }

    public final void invokeBeforeCreate() {
        beforeCreate();
    }

    public final void invokeAfterCreate() {
        afterCreate();
    }

    public final void invokeBeforeUpdate() {
        beforeUpdate();
    }

    public final void invokeAfterUpdate() {
        afterUpdate();
    }
}
