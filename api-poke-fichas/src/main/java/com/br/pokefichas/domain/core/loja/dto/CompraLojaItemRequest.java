package com.br.pokefichas.domain.core.loja.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CompraLojaItemRequest(@NotNull Long idItem, @NotNull @Positive Integer quantidade) { }
