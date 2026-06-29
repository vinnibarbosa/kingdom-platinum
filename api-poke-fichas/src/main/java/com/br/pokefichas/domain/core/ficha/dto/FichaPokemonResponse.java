package com.br.pokefichas.domain.core.ficha.dto;

import java.util.List;

public record FichaPokemonResponse(
        Long id,
        String box,
        String pokebola,
        String apelido,
        String especie,
        String sprite,
        String genero,
        String sobre,
        String ability,
        String feature,
        String mecanica,
        String nature,
        String holdItem,
        String holdItemIcon,
        Integer happinessAtual,
        Integer happinessMax,
        String combo,
        Integer miniUpgrade,
        Integer slotUpgrade,
        Integer hp,
        Integer atk,
        Integer def,
        Integer satk,
        Integer sdef,
        Integer speed,
        Integer pwr,
        Integer stm,
        Integer skl,
        Integer jmp,
        Integer contestSpeed,
        Integer ordem,
        List<FichaPokemonMovimentoResponse> movimentos
) {
}
