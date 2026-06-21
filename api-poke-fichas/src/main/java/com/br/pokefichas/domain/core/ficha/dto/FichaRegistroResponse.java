package com.br.pokefichas.domain.core.ficha.dto;

import java.time.LocalDate;
import java.time.Instant;

public record FichaRegistroResponse(
        Long id,
        String tipoMovimento,
        String descricao,
        LocalDate dataRegistro,
        Instant registradoEm,
        String registradoPor,
        Integer ordem
) {
}
