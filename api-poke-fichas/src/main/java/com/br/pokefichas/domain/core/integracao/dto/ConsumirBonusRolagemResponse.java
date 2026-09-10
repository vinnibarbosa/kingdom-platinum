package com.br.pokefichas.domain.core.integracao.dto;

public record ConsumirBonusRolagemResponse(
        Long idFicha,
        String nomeFicha,
        int quantidadeHoneyRestante,
        int quantidadeShinyCharmRestante,
        boolean operacaoRepetida
) { }
