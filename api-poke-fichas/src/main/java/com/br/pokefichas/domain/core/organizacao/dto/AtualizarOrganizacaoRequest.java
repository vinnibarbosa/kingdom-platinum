package com.br.pokefichas.domain.core.organizacao.dto;

import jakarta.validation.constraints.NotBlank;

public record AtualizarOrganizacaoRequest(
        @NotBlank(message = "Nome da organizacao e obrigatorio")
        String nome,
        Boolean ativo
) {
}
