package com.br.pokefichas.domain.core.loja.dto;

import com.br.pokefichas.domain.core.ficha.dto.FichaItemResponse;

import java.math.BigDecimal;

public record CompraLojaResponse(
        Long idFicha,
        BigDecimal dinheiroRestante,
        FichaItemResponse item,
        BigDecimal totalPago
) { }
