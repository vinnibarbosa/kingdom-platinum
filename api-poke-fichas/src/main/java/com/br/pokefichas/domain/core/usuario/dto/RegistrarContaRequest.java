package com.br.pokefichas.domain.core.usuario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistrarContaRequest(

    @NotBlank(message = "Usuario e obrigatorio")
    @Size(min = 3, max = 50, message = "Usuario deve ter entre 3 e 50 caracteres")
    String username,

    @NotBlank(message = "Senha e obrigatoria")
    @Size(min = 4, max = 100, message = "Senha deve ter entre 4 e 100 caracteres")
    String senha
) {}
