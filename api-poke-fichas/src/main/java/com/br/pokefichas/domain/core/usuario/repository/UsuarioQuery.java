package com.br.pokefichas.domain.core.usuario.repository;

import com.br.pokefichas.commons.dto.PageRequest;
import com.br.pokefichas.commons.page.Page;
import com.br.pokefichas.commons.page.Pageable;
import com.br.pokefichas.commons.page.Sort;
import com.br.pokefichas.commons.persistence.JpaRepository;
import com.br.pokefichas.domain.core.usuario.model.Usuario;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.br.pokefichas.domain.core.entidade.model.QEntidade.entidade;
import static com.br.pokefichas.domain.core.usuario.model.QUsuario.usuario;

@Component
public class UsuarioQuery {

    private final JpaRepository repository;
    private final JPAQueryFactory queryFactory;

    public UsuarioQuery(final JpaRepository repository,
                        final JPAQueryFactory queryFactory) {
        this.repository = repository;
        this.queryFactory = queryFactory;
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

    public Optional<Usuario> findForAuthentication(final String username) {
        final Tuple result = queryFactory
                .select(usuario, entidade.idOrganizacao)
                .from(usuario)
                .join(entidade).on(entidade.id.eq(usuario.idEntidade))
                .where(usuario.username.eq(username))
                .fetchOne();

        return toAuthenticationUser(result);
    }

    public Optional<Usuario> findByIdAndIdEntidadeWithoutTenant(final Long id, final Long idEntidade) {
        return repository.findUniqueOptionalWithoutTenantFilter(
                Usuario.class,
                usuario.id.eq(id).and(usuario.idEntidade.eq(idEntidade))
        );
    }

    public Optional<Usuario> findForAuthentication(final Long id, final Long idEntidade) {
        final Tuple result = queryFactory
                .select(usuario, entidade.idOrganizacao)
                .from(usuario)
                .join(entidade).on(entidade.id.eq(usuario.idEntidade))
                .where(usuario.id.eq(id).and(usuario.idEntidade.eq(idEntidade)))
                .fetchOne();

        return toAuthenticationUser(result);
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

    private Optional<Usuario> toAuthenticationUser(final Tuple result) {
        if (result == null) {
            return Optional.empty();
        }

        final Usuario authenticatedUser = result.get(usuario);
        final Long idOrganizacao = result.get(entidade.idOrganizacao);
        if (authenticatedUser == null || idOrganizacao == null) {
            return Optional.empty();
        }

        authenticatedUser.setIdOrganizacao(idOrganizacao);
        return Optional.of(authenticatedUser);
    }
}
