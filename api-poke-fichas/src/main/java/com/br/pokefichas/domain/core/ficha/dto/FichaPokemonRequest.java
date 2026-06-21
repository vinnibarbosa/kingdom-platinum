package com.br.pokefichas.domain.core.ficha.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.List;

public record FichaPokemonRequest(
        @Size(max = 80)
        String box,
        @Size(max = 80)
        String pokebola,
        @NotBlank(message = "Apelido do Pokemon e obrigatorio")
        @Size(max = 120)
        String apelido,
        @NotBlank(message = "Especie do Pokemon e obrigatoria")
        @Size(max = 120)
        String especie,
        String sprite,
        @Size(max = 20)
        String genero,
        String sobre,
        @Size(max = 120)
        String ability,
        @Size(max = 120)
        String feature,
        @Size(max = 80)
        String mecanica,
        @Size(max = 120)
        String nature,
        @Size(max = 120)
        String holdItem,
        @PositiveOrZero
        Integer happinessAtual,
        @PositiveOrZero
        Integer happinessMax,
        String combo,
        @PositiveOrZero
        Integer miniUpgrade,
        @PositiveOrZero
        Integer slotUpgrade,
        @PositiveOrZero
        Integer hp,
        @PositiveOrZero
        Integer atk,
        @PositiveOrZero
        Integer def,
        @PositiveOrZero
        Integer satk,
        @PositiveOrZero
        Integer sdef,
        @PositiveOrZero
        Integer speed,
        @PositiveOrZero
        Integer pwr,
        @PositiveOrZero
        Integer stm,
        @PositiveOrZero
        Integer skl,
        @PositiveOrZero
        Integer jmp,
        @PositiveOrZero
        Integer contestSpeed,
        @PositiveOrZero
        Integer ordem,
        List<@Valid FichaPokemonMovimentoRequest> movimentos
) {
}
