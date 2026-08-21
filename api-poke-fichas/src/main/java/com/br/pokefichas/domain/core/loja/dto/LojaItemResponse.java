package com.br.pokefichas.domain.core.loja.dto;

import java.math.BigDecimal;

public record LojaItemResponse(
        Long id,
        String categoria,
        String codigo,
        String icone,
        String nome,
        String descricao,
        BigDecimal preco,
        boolean ativo,
        Integer ordem
) { }
