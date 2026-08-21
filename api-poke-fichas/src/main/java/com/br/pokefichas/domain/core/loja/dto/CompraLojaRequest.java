package com.br.pokefichas.domain.core.loja.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CompraLojaRequest(
        @NotNull Long idItem,
        @NotNull Long idFicha,
        @NotNull @Positive Integer quantidade
) { }
