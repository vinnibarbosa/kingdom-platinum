package com.br.pokefichas.commons.tenant;

import com.br.pokefichas.commons.exception.UnauthorizedException;

import java.util.Optional;

public interface TenantContext {

    Optional<Long> getCurrentTenantId();

    default Long getRequiredTenantId() {
        return getCurrentTenantId()
                .orElseThrow(() -> new UnauthorizedException("Tenant atual nao identificado"));
    }

    default boolean hasTenant() {
        return getCurrentTenantId().isPresent();
    }
}
