package com.br.pokefichas.domain.core.usuario.dto;

import com.br.pokefichas.domain.core.usuario.model.enums.Perfil;

import java.time.Instant;

public record UsuarioResponse(
    Long id,
    Long idEntidade,
    String username,
    String nome,
    Perfil perfil,
    Boolean ativo,
    Instant createdAt,
    String createdBy,
    Instant updatedAt,
    String updatedBy
) {}
