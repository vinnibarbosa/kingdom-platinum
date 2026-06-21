package com.br.pokefichas.domain.core.ficha.dto;

public record FichaItemResponse(
        Long id,
        String categoria,
        String codigo,
        String icone,
        String nome,
        Integer quantidade,
        String descricao,
        Integer ordem
) {
}
