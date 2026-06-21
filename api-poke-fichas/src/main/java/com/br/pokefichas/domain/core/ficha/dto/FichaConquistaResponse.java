package com.br.pokefichas.domain.core.ficha.dto;

import java.time.LocalDate;

public record FichaConquistaResponse(
        Long id,
        String tipo,
        String nome,
        String imagem,
        LocalDate dataConquista,
        Integer ordem
) {
}
