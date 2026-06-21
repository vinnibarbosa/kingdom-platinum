package com.br.pokefichas.commons.security.usecase;

import com.br.pokefichas.commons.exception.AuthenticationException;
import com.br.pokefichas.commons.security.JwtTokenProvider;
import com.br.pokefichas.commons.security.dto.AuthResponse;
import com.br.pokefichas.commons.security.dto.RefreshTokenRequest;
import com.br.pokefichas.commons.security.dto.UsuarioAuthInfo;
import com.br.pokefichas.commons.security.mapper.AuthMapper;
import com.br.pokefichas.commons.security.model.RefreshToken;
import com.br.pokefichas.commons.security.repository.RefreshTokenQuery;
import com.br.pokefichas.commons.security.service.RefreshTokenService;
import com.br.pokefichas.domain.core.entidade.model.Entidade;
import com.br.pokefichas.domain.core.entidade.repository.EntidadeQuery;
import com.br.pokefichas.domain.core.usuario.model.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshTokenUseCase {

    private final RefreshTokenQuery refreshTokenQuery;
    private final RefreshTokenService refreshTokenService;
    private final EntidadeQuery entidadeQuery;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthMapper authMapper;

    public RefreshTokenUseCase(final RefreshTokenQuery refreshTokenQuery,
                               final RefreshTokenService refreshTokenService,
                               final EntidadeQuery entidadeQuery,
                               final JwtTokenProvider jwtTokenProvider,
                               final AuthMapper authMapper) {
        this.refreshTokenQuery = refreshTokenQuery;
        this.refreshTokenService = refreshTokenService;
        this.entidadeQuery = entidadeQuery;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authMapper = authMapper;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public AuthResponse handle(final RefreshTokenRequest request) {
        final RefreshToken oldRefreshToken = refreshTokenQuery.findByToken(request.refreshToken())
                .orElseThrow(AuthenticationException::invalidToken);

        refreshTokenService.verifyExpiration(oldRefreshToken);

        final Usuario usuario = oldRefreshToken.getUsuario();

        if (usuario == null) {
            throw AuthenticationException.invalidToken();
        }

        if (!usuario.isAtivo()) {
            throw AuthenticationException.invalidCredentials();
        }

        if (usuario.getIdEntidade() == null) {
            throw AuthenticationException.invalidCredentials();
        }

        final Entidade entidade = entidadeQuery.findByIdWithoutContext(usuario.getIdEntidade())
                .orElseThrow(AuthenticationException::invalidCredentials);
        usuario.setIdOrganizacao(entidade.getIdOrganizacao());

        final RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(usuario);
        final String newAccessToken = jwtTokenProvider.generateAccessToken(usuario);
        final UsuarioAuthInfo usuarioInfo = authMapper.toUsuarioAuthInfo(usuario);

        return AuthResponse.refreshResponse(
                newAccessToken,
                newRefreshToken.getToken(),
                jwtTokenProvider.getAccessTokenExpirationInSeconds(),
                usuarioInfo
        );
    }
}
