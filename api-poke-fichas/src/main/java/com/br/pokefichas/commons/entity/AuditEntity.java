package com.br.pokefichas.commons.entity;

import java.time.Instant;

public interface AuditEntity {
    String getCreatedBy();
    void setCreatedBy(String createdBy);
    String getUpdatedBy();
    void setUpdatedBy(String updatedBy);
    Instant getCreatedAt();
    void setCreatedAt(Instant createdAt);
    Instant getUpdatedAt();
    void setUpdatedAt(Instant updatedAt);
}
