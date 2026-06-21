package com.br.pokefichas.domain.core.ficha.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.Instant;

public record FichaRegistroRequest(
        @NotBlank(message = "Tipo do registro e obrigatorio")
        @Size(max = 20)
        String tipoMovimento,
        @NotBlank(message = "Descricao do registro e obrigatoria")
        String descricao,
        LocalDate dataRegistro,
        Instant registradoEm,
        @Size(max = 255)
        String registradoPor,
        @PositiveOrZero
        Integer ordem
) {
}
