package com.br.pokefichas.commons.security.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta de autenticação com tokens JWT")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthResponse(

        @Schema(description = "Access token JWT")
        String accessToken,

        @JsonIgnore
        @Schema(hidden = true)
        String refreshToken,

        @Schema(description = "Tipo do token", example = "Bearer")
        String tokenType,

        @Schema(description = "Tempo de expiração em segundos")
        Long expiresIn,

        @Schema(description = "Informações do usuário autenticado")
        UsuarioAuthInfo usuario
) {
    public static AuthResponse of(final String accessToken,
                                  final String refreshToken,
                                  final Long expiresIn,
                                  final UsuarioAuthInfo usuario) {
        return new AuthResponse(accessToken, refreshToken, "Bearer", expiresIn, usuario);
    }

    public static AuthResponse refreshResponse(final String accessToken,
                                               final String refreshToken,
                                               final Long expiresIn,
                                               final UsuarioAuthInfo usuario) {
        return new AuthResponse(accessToken, refreshToken, "Bearer", expiresIn, usuario);
    }
}
