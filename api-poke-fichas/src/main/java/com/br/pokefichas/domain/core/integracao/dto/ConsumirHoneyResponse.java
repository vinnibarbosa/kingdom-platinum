package com.br.pokefichas.domain.core.integracao.dto;

public record ConsumirHoneyResponse(
        Long idFicha,
        String nomeFicha,
        int quantidadeConsumida,
        int quantidadeRestante,
        int bonusEncontros,
        boolean operacaoRepetida
) { }
