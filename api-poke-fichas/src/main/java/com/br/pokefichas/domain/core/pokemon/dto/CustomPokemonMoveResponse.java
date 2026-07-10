package com.br.pokefichas.domain.core.pokemon.dto;

public record CustomPokemonMoveResponse(
        String name,
        String category,
        String type,
        String style,
        Integer power,
        Integer accuracy
) {
}
