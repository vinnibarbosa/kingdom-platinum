package com.br.pokefichas.domain.core.entidade.dto;

import jakarta.validation.constraints.NotBlank;

public record AtualizarEntidadeRequest(
        Long idOrganizacao,
        @NotBlank(message = "Nome da entidade e obrigatorio")
        String nome,
        String nomeFantasia,
        String inscricaoEstadual,
        String telefone,
        Boolean ativo
) {
}
