package com.br.pokefichas.domain.core.usuario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RedefinirSenhaRequest(

        @NotBlank(message = "Usuario e obrigatorio")
        @Size(min = 3, max = 50, message = "Usuario deve ter entre 3 e 50 caracteres")
        String username
) {}
