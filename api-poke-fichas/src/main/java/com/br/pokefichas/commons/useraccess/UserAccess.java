package com.br.pokefichas.commons.useraccess;

import com.br.pokefichas.commons.useraccess.provider.UserAccessProvider;
import com.br.pokefichas.domain.core.usuario.model.Usuario;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

@Service
public class UserAccess {

    private final UserAccessProvider provider;

    public UserAccess(final UserAccessProvider provider) {
        this.provider = provider;
    }

    public Optional<UserAccessDTO> getCurrentUser() {
        return getAuthentication()
                .filter(this::isValidAuthentication)
                .map(authentication -> (Usuario) authentication.getPrincipal())
                .map(this::createUserAccessDTO);
    }

    public Optional<String> getUsername() {
        return getCurrentUser().map(UserAccessDTO::username);
    }

    public Optional<String> getRole() {
        return getCurrentUser().map(UserAccessDTO::role);
    }

    public Optional<Long> getIdEntidade() {
        return getCurrentUser().map(UserAccessDTO::idEntidade);
    }

    public Optional<Long> getIdOrganizacao() {
        return getCurrentUser().map(UserAccessDTO::idOrganizacao);
    }

    public boolean isAuthenticated() {
        return getCurrentUser()
                .map(userAccessDTO -> true)
                .orElse(false);
    }

    public String getUsernameOrNull() {
        return getUsername().orElse(null);
    }

    public <T> Optional<T> getPropertyFromCurrentUser(final Function<UserAccessDTO, T> propertyExtractor) {
        return getCurrentUser().map(propertyExtractor);
    }

    public void ifAuthenticated(final Consumer<UserAccessDTO> action) {
        getCurrentUser().ifPresent(action);
    }

    private Optional<Authentication> getAuthentication() {
        return Optional.ofNullable(provider.getAuthentication());
    }

    private boolean isValidAuthentication(final Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof Usuario
                && !"anonymousUser".equals(authentication.getPrincipal());
    }

    private UserAccessDTO createUserAccessDTO(final Usuario usuario) {
        return new UserAccessDTO(
                usuario.getUsername(),
                usuario.getNome(),
                usuario.getIdEntidade(),
                usuario.getIdOrganizacao(),
                usuario.getPerfil().name(),
                true
        );
    }
}
