package com.br.pokefichas.domain.core.entidade.dto;

import java.time.Instant;

public record EntidadeResponse(
        Long id,
        Long idOrganizacao,
        String nome,
        String nomeFantasia,
        String inscricaoEstadual,
        String telefone, Boolean ativo,
        Instant createdAt,
        String createdBy,
        Instant updatedAt,
        String updatedBy
) {
}
