package com.br.pokefichas.domain.core.integracao.dto;

public record ConsumirShinyCharmResponse(
        Long idFicha,
        String nomeFicha,
        int quantidadeConsumida,
        int quantidadeRestante,
        boolean operacaoRepetida
) { }
