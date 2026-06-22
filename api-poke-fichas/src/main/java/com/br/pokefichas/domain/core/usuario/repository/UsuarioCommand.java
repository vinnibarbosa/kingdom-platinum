package com.br.pokefichas.domain.core.usuario.repository;

import com.br.pokefichas.commons.persistence.JpaRepository;
import com.br.pokefichas.domain.core.usuario.model.Usuario;
import org.springframework.stereotype.Component;

@Component
public class UsuarioCommand {

    private final JpaRepository repository;

    public UsuarioCommand(final JpaRepository repository) {
        this.repository = repository;
    }

    public Usuario save(final Usuario usuario) {
        return repository.save(usuario);
    }

    public void markPasswordResetPendingWithoutTenant(final Long id, final int authVersion) {
        repository.updateWithAuditWithoutTenantFilter(
                Usuario.class,
                update -> update
                        .set(com.br.pokefichas.domain.core.usuario.model.QUsuario.usuario.senhaRedefinicaoPendente, true)
                        .set(com.br.pokefichas.domain.core.usuario.model.QUsuario.usuario.authVersion, authVersion),
                com.br.pokefichas.domain.core.usuario.model.QUsuario.usuario.id.eq(id)
        );
    }

    public boolean claimPendingPasswordWithoutTenant(final Long id, final String senha) {
        final long updated = repository.updateWithAuditWithoutTenantFilter(
                Usuario.class,
                update -> update
                        .set(com.br.pokefichas.domain.core.usuario.model.QUsuario.usuario.senha, senha)
                        .set(com.br.pokefichas.domain.core.usuario.model.QUsuario.usuario.senhaRedefinicaoPendente, false),
                com.br.pokefichas.domain.core.usuario.model.QUsuario.usuario.id.eq(id),
                com.br.pokefichas.domain.core.usuario.model.QUsuario.usuario.senhaRedefinicaoPendente.isTrue()
        );
        return updated == 1;
    }
}
