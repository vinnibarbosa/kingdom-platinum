package com.br.pokefichas.commons.security.dto;

import com.br.pokefichas.domain.core.usuario.model.enums.Perfil;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Informacoes do usuario autenticado")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UsuarioAuthInfo(

        @Schema(description = "ID do usuario")
        Long id,

        @Schema(description = "ID da entidade atual")
        Long idEntidade,

        @Schema(description = "ID da organizacao atual")
        Long idOrganizacao,

        @Schema(description = "Username")
        String username,

        @Schema(description = "Nome completo")
        String nome,

        @Schema(description = "Perfil de acesso")
        Perfil perfil,

        @Schema(description = "Descricao do perfil")
        String perfilDescricao,

        @Schema(description = "Se o usuario esta ativo")
        boolean ativo,

        @Schema(description = "Se esta autenticado")
        boolean isAuthenticated
) {}
