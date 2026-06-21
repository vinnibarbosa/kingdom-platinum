package com.br.pokefichas.domain.core.organizacao.dto;

import jakarta.validation.constraints.NotBlank;

public record CriarOrganizacaoRequest(
        @NotBlank(message = "Nome da organizacao e obrigatorio")
        String nome,
        Boolean ativo
) {
}
