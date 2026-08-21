package com.br.pokefichas.domain.core.loja.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record LojaItemRequest(
        @NotBlank @Size(max = 60) String categoria,
        @Size(max = 80) String codigo,
        String icone,
        @NotBlank @Size(max = 150) String nome,
        String descricao,
        @NotNull @DecimalMin(value = "0.00") BigDecimal preco,
        Boolean ativo,
        Integer ordem
) { }
