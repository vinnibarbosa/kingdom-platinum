package com.br.pokefichas.domain.core.integracao.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ConsumirHoneyRequest(
        @NotNull Long idFicha,
        @NotNull UUID idOperacao
) { }
