package com.br.pokefichas.domain.core.usuario.repository;

import com.br.pokefichas.commons.dto.PageRequest;
import com.br.pokefichas.commons.page.Page;
import com.br.pokefichas.commons.page.Pageable;
import com.br.pokefichas.commons.page.Sort;
import com.br.pokefichas.commons.persistence.JpaRepository;
import com.br.pokefichas.domain.core.usuario.model.Usuario;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.br.pokefichas.domain.core.usuario.model.QUsuario.usuario;

@Component
public class UsuarioQuery {

    private final JpaRepository repository;

    public UsuarioQuery(final JpaRepository repository) {
        this.repository = repository;
    }

    public Optional<Usuario> findById(final Long id) {
        return repository.findOptional(Usuario.class, id);
    }

    public Optional<Usuario> findByUsername(final String username) {
        return repository.findUniqueOptional(Usuario.class, usuario.username.eq(username));
    }

    public Optional<Usuario> findByUsernameWithoutTenant(final String username) {
        return repository.findUniqueOptionalWithoutTenantFilter(Usuario.class, usuario.username.eq(username));
    }

    public Optional<Usuario> findByIdAndIdEntidadeWithoutTenant(final Long id, final Long idEntidade) {
        return repository.findUniqueOptionalWithoutTenantFilter(
                Usuario.class,
                usuario.id.eq(id).and(usuario.idEntidade.eq(idEntidade))
        );
    }

    public Page<Usuario> findAll(final PageRequest pageRequest) {
        final Pageable pageable = pageRequest.toPageable(Sort.of(usuario.nome.asc()));
        return repository.findAll(Usuario.class, pageable);
    }

    public boolean existsByUsername(final String username) {
        return repository.existsWithoutTenant(Usuario.class, usuario.username.eq(username));
    }

    public boolean existsAnyWithoutTenant() {
        return repository.existsWithoutTenant(Usuario.class);
    }
}
