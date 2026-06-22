package com.br.pokefichas.domain.core.ficha.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record FichaPokemonMovimentoRequest(
        @NotBlank(message = "Nome do movimento e obrigatorio")
        @Size(max = 120)
        String nome,
        @Size(max = 40)
        String categoria,
        @Size(max = 40)
        String tipo,
        @Size(max = 20)
        String style,
        @PositiveOrZero
        Integer poder,
        @PositiveOrZero
        Integer accuracy,
        @PositiveOrZero
        Integer ordem
) {
}
