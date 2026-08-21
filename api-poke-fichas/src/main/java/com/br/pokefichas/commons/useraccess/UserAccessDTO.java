package com.br.pokefichas.commons.useraccess;

public record UserAccessDTO(
        Long id,
        String username,
        String name,
        Long idEntidade,
        Long idOrganizacao,
        String role,
        boolean isAuthenticated
) {}
