package com.br.pokefichas.domain.core.loja.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CompraLojaRequest(@NotNull Long idFicha, @NotEmpty List<@Valid CompraLojaItemRequest> itens, String cupom) { }
