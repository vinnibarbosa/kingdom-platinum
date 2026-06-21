package com.br.pokefichas.domain.core.usuario.dto;

import com.br.pokefichas.domain.core.usuario.model.enums.Perfil;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CriarUsuarioRequest(

    @NotBlank(message = "Username e obrigatorio")
    @Size(min = 3, max = 50, message = "Username deve ter entre 3 e 50 caracteres")
    String username,

    @NotBlank(message = "Nome e obrigatorio")
    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
    String nome,

    @NotBlank(message = "Senha e obrigatoria")
    @Size(min = 4, max = 100, message = "Senha deve ter entre 4 e 100 caracteres")
    String senha,

    Perfil perfil,

    Boolean ativo
) {}
