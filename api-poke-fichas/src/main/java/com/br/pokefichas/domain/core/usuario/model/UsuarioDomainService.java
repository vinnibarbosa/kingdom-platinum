package com.br.pokefichas.domain.core.usuario.model;

import com.br.pokefichas.commons.exception.BusinessException;
import com.br.pokefichas.domain.core.usuario.repository.UsuarioQuery;
import org.springframework.stereotype.Component;

@Component
public class UsuarioDomainService {

    private final UsuarioQuery query;

    public UsuarioDomainService(final UsuarioQuery query) {
        this.query = query;
    }

    public void validarPerfilObrigatorio(final Usuario usuario) {
        if (usuario.getPerfil() == null) {
            throw new BusinessException("Perfil e obrigatorio");
        }
    }

    public void validarTenantObrigatorio(final Usuario usuario) {
        if (usuario.getIdEntidade() == null) {
            throw new BusinessException("idEntidade e obrigatorio para usuario");
        }
    }

    public void validarUnicidadeUsernameCriacao(final Usuario usuario) {
        if (query.existsByUsername(usuario.getUsername())) {
            throw new BusinessException("Este nome de usuário já está em uso");
        }
    }

}
