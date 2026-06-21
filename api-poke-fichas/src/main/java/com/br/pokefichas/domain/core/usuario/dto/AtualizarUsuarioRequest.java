package com.br.pokefichas.domain.core.usuario.dto;

import com.br.pokefichas.domain.core.usuario.model.enums.Perfil;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AtualizarUsuarioRequest(

    @NotBlank(message = "Nome e obrigatorio")
    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
    String nome,

    @Size(min = 4, max = 100, message = "Senha deve ter entre 4 e 100 caracteres")
    String senha,

    @NotNull(message = "Perfil e obrigatorio")
    Perfil perfil,

    @NotNull(message = "Status ativo e obrigatorio")
    Boolean ativo
) {}
