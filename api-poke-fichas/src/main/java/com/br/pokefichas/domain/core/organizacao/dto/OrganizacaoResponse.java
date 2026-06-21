package com.br.pokefichas.domain.core.organizacao.dto;

import java.time.Instant;

public record OrganizacaoResponse(
        Long id,
        String nome,
        Boolean ativo,
        Instant createdAt,
        String createdBy,
        Instant updatedAt,
        String updatedBy
) {
}
