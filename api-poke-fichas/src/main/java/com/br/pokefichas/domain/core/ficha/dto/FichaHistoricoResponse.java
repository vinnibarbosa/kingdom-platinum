package com.br.pokefichas.domain.core.ficha.dto;

import java.time.Instant;

public record FichaHistoricoResponse(
        Long id,
        String lote,
        String acao,
        String campo,
        String valorAnterior,
        String valorNovo,
        Instant createdAt,
        String createdBy
) {
}
