package com.br.pokefichas.commons.security.usecase;

import com.br.pokefichas.commons.exception.AuthenticationException;
import com.br.pokefichas.commons.security.JwtTokenProvider;
import com.br.pokefichas.commons.security.dto.AuthRequest;
import com.br.pokefichas.commons.security.dto.AuthResponse;
import com.br.pokefichas.commons.security.dto.UsuarioAuthInfo;
import com.br.pokefichas.commons.security.mapper.AuthMapper;
import com.br.pokefichas.commons.security.model.RefreshToken;
import com.br.pokefichas.commons.security.service.RefreshTokenService;
import com.br.pokefichas.domain.core.entidade.model.Entidade;
import com.br.pokefichas.domain.core.entidade.repository.EntidadeQuery;
import com.br.pokefichas.domain.core.usuario.model.Usuario;
import com.br.pokefichas.domain.core.usuario.repository.UsuarioQuery;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class LoginUseCase {

    private final UsuarioQuery usuarioQuery;
    private final EntidadeQuery entidadeQuery;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final AuthMapper authMapper;

    public LoginUseCase(final UsuarioQuery usuarioQuery,
                        final EntidadeQuery entidadeQuery,
                        final PasswordEncoder passwordEncoder,
                        final JwtTokenProvider jwtTokenProvider,
                        final RefreshTokenService refreshTokenService,
                        final AuthMapper authMapper) {
        this.usuarioQuery = usuarioQuery;
        this.entidadeQuery = entidadeQuery;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenService = refreshTokenService;
        this.authMapper = authMapper;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public AuthResponse handle(final AuthRequest request) {
        final Usuario usuario = usuarioQuery.findByUsernameWithoutTenant(request.username())
                .orElseThrow(AuthenticationException::invalidCredentials);

        if (!usuario.isAtivo()) {
            throw AuthenticationException.invalidCredentials();
        }

        if (!passwordEncoder.matches(request.senha(), usuario.getSenha())) {
            throw AuthenticationException.invalidCredentials();
        }

        if (usuario.getIdEntidade() == null) {
            throw AuthenticationException.invalidCredentials();
        }

        final Entidade entidade = entidadeQuery.findByIdWithoutContext(usuario.getIdEntidade())
                .orElseThrow(AuthenticationException::invalidCredentials);
        usuario.setIdOrganizacao(entidade.getIdOrganizacao());

        final String accessToken = jwtTokenProvider.generateAccessToken(usuario);
        final RefreshToken refreshToken = refreshTokenService.createRefreshToken(usuario);
        final UsuarioAuthInfo usuarioInfo = authMapper.toUsuarioAuthInfo(usuario);

        return AuthResponse.of(
                accessToken,
                refreshToken.getToken(),
                jwtTokenProvider.getAccessTokenExpirationInSeconds(),
                usuarioInfo
        );
    }
}
