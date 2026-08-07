package com.br.pokefichas.domain.core.ficha.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FichaTimelineEntryRequest(
        @NotBlank @Size(max = 80) String secao,
        @Size(max = 120) String periodo,
        @NotBlank @Size(max = 180) String titulo,
        @Size(max = 255) String subtitulo,
        @NotBlank String conteudo,
        @Size(max = 24) String cor,
        Integer ordem
) {
}
