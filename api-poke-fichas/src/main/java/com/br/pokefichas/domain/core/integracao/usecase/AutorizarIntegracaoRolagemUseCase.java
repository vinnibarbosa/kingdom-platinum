package com.br.pokefichas.domain.core.integracao.usecase;

import com.br.pokefichas.commons.exception.AuthenticationException;
import com.br.pokefichas.commons.security.JwtTokenProvider;
import com.br.pokefichas.commons.useraccess.UserAccess;
import com.br.pokefichas.domain.core.integracao.dto.CriarAutorizacaoRolagemResponse;
import com.br.pokefichas.domain.core.integracao.dto.SessaoRolagemResponse;
import com.br.pokefichas.domain.core.integracao.dto.TrocarAutorizacaoRolagemRequest;
import com.br.pokefichas.domain.core.integracao.model.IntegracaoAutorizacao;
import com.br.pokefichas.domain.core.integracao.repository.IntegracaoAutorizacaoRepository;
import com.br.pokefichas.domain.core.usuario.model.Usuario;
import com.br.pokefichas.domain.core.usuario.repository.UsuarioQuery;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

@Component
public class AutorizarIntegracaoRolagemUseCase {

    private static final int CODE_VALIDITY_MINUTES = 2;

    private final SecureRandom secureRandom = new SecureRandom();
    private final IntegracaoAutorizacaoRepository repository;
    private final UsuarioQuery usuarioQuery;
    private final UserAccess userAccess;
    private final JwtTokenProvider tokenProvider;

    public AutorizarIntegracaoRolagemUseCase(final IntegracaoAutorizacaoRepository repository,
                                             final UsuarioQuery usuarioQuery,
                                             final UserAccess userAccess,
                                             final JwtTokenProvider tokenProvider) {
        this.repository = repository;
        this.usuarioQuery = usuarioQuery;
        this.userAccess = userAccess;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public CriarAutorizacaoRolagemResponse criar() {
        final Long idUsuario = userAccess.getId().orElseThrow(AuthenticationException::invalidToken);
        final Long idEntidade = userAccess.getIdEntidade().orElseThrow(AuthenticationException::invalidToken);
        final Long idOrganizacao = userAccess.getIdOrganizacao().orElseThrow(AuthenticationException::invalidToken);
        final byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        final String code = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        final Instant expiresAt = Instant.now().plus(CODE_VALIDITY_MINUTES, ChronoUnit.MINUTES);

        repository.save(IntegracaoAutorizacao.Builder.create()
                .idUsuario(idUsuario)
                .idEntidade(idEntidade)
                .idOrganizacao(idOrganizacao)
                .codigoHash(hash(code))
                .expiraEm(expiresAt)
                .build());
        return new CriarAutorizacaoRolagemResponse(code, expiresAt);
    }

    @Transactional
    public SessaoRolagemResponse trocar(final TrocarAutorizacaoRolagemRequest request) {
        final IntegracaoAutorizacao authorization = repository.findForUpdateByHash(hash(request.codigo()))
                .orElseThrow(AuthenticationException::invalidToken);
        if (authorization.getUsadoEm() != null || authorization.getExpiraEm().isBefore(Instant.now())) {
            throw AuthenticationException.invalidToken();
        }

        final Usuario usuario = usuarioQuery.findForAuthentication(
                        authorization.getIdUsuario(), authorization.getIdEntidade())
                .filter(Usuario::isAtivo)
                .orElseThrow(AuthenticationException::invalidToken);
        if (!authorization.getIdOrganizacao().equals(usuario.getIdOrganizacao())) {
            throw AuthenticationException.invalidToken();
        }

        repository.save(IntegracaoAutorizacao.Builder.from(authorization).usadoEm(Instant.now()).build());
        return new SessaoRolagemResponse(
                tokenProvider.generateRollIntegrationToken(usuario),
                "Bearer",
                tokenProvider.getAccessTokenExpirationInSeconds(),
                usuario.getId(),
                usuario.getUsername(),
                usuario.getNome()
        );
    }

    private String hash(final String value) {
        try {
            final byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponivel", e);
        }
    }
}
