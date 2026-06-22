package com.br.pokefichas.commons.security;

import com.br.pokefichas.domain.core.usuario.repository.UsuarioQuery;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioQuery usuarioQuery;

    public UsuarioDetailsService(final UsuarioQuery usuarioQuery) {
        this.usuarioQuery = usuarioQuery;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(final String username) {
        return usuarioQuery.findByUsernameWithoutTenant(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario nao encontrado: " + username));
    }

    @Transactional(readOnly = true)
    public UserDetails loadByIdAndTenant(final Long idUsuario, final Long idEntidade) {
        return usuarioQuery.findForAuthentication(idUsuario, idEntidade)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario nao encontrado para id " + idUsuario + " no tenant " + idEntidade
                ));
    }
}
