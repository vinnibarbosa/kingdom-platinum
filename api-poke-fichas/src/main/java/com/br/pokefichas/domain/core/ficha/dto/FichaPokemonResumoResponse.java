package com.br.pokefichas.domain.core.ficha.dto;

public record FichaPokemonResumoResponse(
        String apelido,
        String especie,
        String sprite,
        String mecanica,
        Integer ordem
) {
}
