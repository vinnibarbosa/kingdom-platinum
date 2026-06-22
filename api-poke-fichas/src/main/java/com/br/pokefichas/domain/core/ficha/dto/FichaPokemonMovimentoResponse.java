package com.br.pokefichas.domain.core.ficha.dto;

public record FichaPokemonMovimentoResponse(
        Long id,
        String nome,
        String categoria,
        String tipo,
        String style,
        Integer poder,
        Integer accuracy,
        Integer ordem
) {
}
