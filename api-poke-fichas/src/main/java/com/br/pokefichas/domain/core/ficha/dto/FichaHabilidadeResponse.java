package com.br.pokefichas.domain.core.ficha.dto;

public record FichaHabilidadeResponse(
        Long id,
        String nome,
        String descricao,
        Integer ordem
) {
}
