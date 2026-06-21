package com.br.pokefichas.commons.useraccess;

public record UserAccessDTO(
        String username,
        String name,
        Long idEntidade,
        Long idOrganizacao,
        String role,
        boolean isAuthenticated
) {}
