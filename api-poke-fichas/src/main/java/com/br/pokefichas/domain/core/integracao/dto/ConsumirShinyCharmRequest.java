package com.br.pokefichas.domain.core.integracao.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ConsumirShinyCharmRequest(
        @NotNull Long idFicha,
        @NotNull UUID idOperacao
) { }
