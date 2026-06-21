package com.br.pokefichas.domain.core.ficha.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record FichaRelacionadoRequest(
        @NotBlank(message = "Nome do relacionado e obrigatorio")
        @Size(max = 150)
        String nome,
        @Size(max = 120)
        String relacao,
        String imagem,
        String historia,
        @PositiveOrZero
        Integer ordem
) {
}
