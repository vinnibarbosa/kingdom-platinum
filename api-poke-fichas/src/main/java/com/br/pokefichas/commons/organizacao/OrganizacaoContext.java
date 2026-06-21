package com.br.pokefichas.commons.organizacao;

import com.br.pokefichas.commons.exception.UnauthorizedException;

import java.util.Optional;

public interface OrganizacaoContext {

    Optional<Long> getCurrentOrganizacaoId();

    default Long getRequiredOrganizacaoId() {
        return getCurrentOrganizacaoId()
                .orElseThrow(() -> new UnauthorizedException("Organizacao atual nao identificada"));
    }

    default boolean hasOrganizacao() {
        return getCurrentOrganizacaoId().isPresent();
    }
}
