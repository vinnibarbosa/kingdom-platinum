package com.br.pokefichas.domain.core.loja.dto;

import java.math.BigDecimal;
import java.util.List;

public record CompraLojaResponse(
        Long idFicha,
        BigDecimal dinheiroRestante,
        List<String> itens,
        BigDecimal subtotal,
        BigDecimal desconto,
        BigDecimal totalPago
) { }
