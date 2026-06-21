package com.br.pokefichas.domain.core.bootstrap.dto;

public record BootstrapResponse(
        Long idOrganizacao,
        Long idEntidade,
        Long idUsuario,
        String username,
        String nomeOrganizacao,
        String nomeEntidade
) {
}
