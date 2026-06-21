package com.br.pokefichas.domain.core.ficha.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record FichaConquistaRequest(
        @NotBlank(message = "Tipo da conquista e obrigatorio")
        @Size(max = 40)
        String tipo,
        @NotBlank(message = "Nome da conquista e obrigatorio")
        @Size(max = 150)
        String nome,
        String imagem,
        LocalDate dataConquista,
        @PositiveOrZero
        Integer ordem
) {
}
