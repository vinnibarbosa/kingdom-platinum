package com.br.pokefichas.domain.core.integracao.dto;

public record SessaoRolagemResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        Long idUsuario,
        String username,
        String nome
) { }
