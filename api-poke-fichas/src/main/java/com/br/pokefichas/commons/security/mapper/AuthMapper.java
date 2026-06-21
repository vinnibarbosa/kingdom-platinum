package com.br.pokefichas.commons.security.mapper;

import com.br.pokefichas.commons.security.dto.UsuarioAuthInfo;
import com.br.pokefichas.commons.utils.ObjectUtil;
import com.br.pokefichas.domain.core.usuario.model.Usuario;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {

    public UsuarioAuthInfo toUsuarioAuthInfo(final Usuario usuario) {
        if (usuario == null) {
            return null;
        }

        return new UsuarioAuthInfo(
                usuario.getId(),
                usuario.getIdEntidade(),
                usuario.getIdOrganizacao(),
                usuario.getUsername(),
                usuario.getNome(),
                usuario.getPerfil(),
                ObjectUtil.getIfExists(usuario.getPerfil(), perfil -> perfil.getDescription()),
                usuario.isAtivo(),
                true
        );
    }
}
