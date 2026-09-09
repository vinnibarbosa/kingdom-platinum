package com.br.pokefichas.domain.core.integracao.dto;

import jakarta.validation.constraints.NotBlank;

public record TrocarAutorizacaoRolagemRequest(@NotBlank String codigo) { }
