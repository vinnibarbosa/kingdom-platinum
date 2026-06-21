package com.br.pokefichas.commons.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Requisicao de autenticacao")
public record AuthRequest(

        @Schema(description = "Username do usuario", example = "joao.silva")
        @NotBlank(message = "Username e obrigatorio")
        String username,

        @Schema(description = "Senha do usuario")
        @NotBlank(message = "Senha e obrigatoria")
        String senha
) {}
