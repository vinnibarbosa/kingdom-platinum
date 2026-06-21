package com.br.pokefichas.domain.core.ficha.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record FichaHabilidadeRequest(
        @NotBlank(message = "Nome da habilidade e obrigatorio")
        @Size(max = 120)
        String nome,
        String descricao,
        @PositiveOrZero
        Integer ordem
) {
}
