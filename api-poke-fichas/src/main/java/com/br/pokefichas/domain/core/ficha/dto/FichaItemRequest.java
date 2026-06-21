package com.br.pokefichas.domain.core.ficha.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record FichaItemRequest(
        @NotBlank(message = "Categoria do item e obrigatoria")
        @Size(max = 60)
        String categoria,
        @Size(max = 40)
        String codigo,
        String icone,
        @NotBlank(message = "Nome do item e obrigatorio")
        @Size(max = 150)
        String nome,
        @PositiveOrZero
        Integer quantidade,
        String descricao,
        @PositiveOrZero
        Integer ordem
) {
}
