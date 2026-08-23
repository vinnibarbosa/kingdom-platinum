package com.br.pokefichas.domain.core.loja.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ImportarCatalogoLojaRequest(
        @NotEmpty List<@Valid LojaItemRequest> itens
) { }
