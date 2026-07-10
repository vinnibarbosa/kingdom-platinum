package com.br.pokefichas.domain.core.pokemon.dto;

import java.util.List;
import java.util.Map;

public record CustomPokemonResponse(
        String name,
        String sprite,
        List<String> types,
        List<String> abilities,
        List<CustomPokemonMoveResponse> moves,
        Map<String, Integer> stats
) {
}
