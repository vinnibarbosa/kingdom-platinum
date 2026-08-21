package com.br.pokefichas.domain.core.loja.dto;

import java.math.BigDecimal;

public record FichaCompraResponse(Long id, String nome, BigDecimal dinheiro, String corTema) { }
