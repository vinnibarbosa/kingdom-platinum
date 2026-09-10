package com.br.pokefichas.domain.core.integracao.dto;

public record FichaRolagemResponse(
        Long id,
        String nome,
        Integer ranking,
        int quantidadeHoney,
        int quantidadeShinyCharm
) { }
