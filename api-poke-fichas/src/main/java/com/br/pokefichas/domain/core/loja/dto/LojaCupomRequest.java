package com.br.pokefichas.domain.core.loja.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LojaCupomRequest(
        @NotBlank @Size(max = 40) String codigo,
        @NotNull @Min(1) @Max(100) Integer percentual,
        Boolean ativo
) { }
