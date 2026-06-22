package com.br.pokefichas.commons.security;

import com.br.pokefichas.commons.exception.AuthenticationException;
import com.br.pokefichas.domain.core.usuario.model.Usuario;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final UsuarioDetailsService usuarioDetailsService;

    public JwtAuthenticationFilter(final JwtTokenProvider jwtTokenProvider,
                                   final UsuarioDetailsService usuarioDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.usuarioDetailsService = usuarioDetailsService;
    }

    @Override
    protected void doFilterInternal(@NonNull final HttpServletRequest request,
                                    @NonNull final HttpServletResponse response,
                                    @NonNull final FilterChain filterChain)
            throws ServletException, IOException {

        final String token = extractTokenFromRequest(request);
        if (token != null) {
            tryAuthenticate(request, token);
        }

        filterChain.doFilter(request, response);
    }

    private void tryAuthenticate(final HttpServletRequest request, final String token) {
        try {
            final JwtTokenProvider.AuthenticationClaims claims =
                    jwtTokenProvider.extractAuthenticationClaims(token);
            final String username = claims.username();
            final Long idUsuario = claims.idUsuario();
            final Long idEntidade = claims.idEntidade();
            final Long idOrganizacao = claims.idOrganizacao();
            if (username == null || idUsuario == null || idEntidade == null || idOrganizacao == null
                    || SecurityContextHolder.getContext().getAuthentication() != null) {
                return;
            }

            final UserDetails userDetails = usuarioDetailsService.loadByIdAndTenant(idUsuario, idEntidade);
            if (!username.equals(userDetails.getUsername())) {
                return;
            }

            if (!(userDetails instanceof Usuario usuario)
                    || !idOrganizacao.equals(usuario.getIdOrganizacao())) {
                log.warn("Claim idOrganizacao={} divergente da Entidade {} no banco | URI: {}",
                        idOrganizacao, idEntidade, request.getRequestURI());
                return;
            }

            final UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (final AuthenticationException e) {
            log.warn("Falha na autenticacao JWT: {} | URI: {}", e.getMessage(), request.getRequestURI());
            SecurityContextHolder.clearContext();
        } catch (final RuntimeException e) {
            log.error("Erro inesperado ao processar token JWT: {} | URI: {}", e.getMessage(), request.getRequestURI(), e);
            SecurityContextHolder.clearContext();
        }
    }

    private String extractTokenFromRequest(final HttpServletRequest request) {
        final String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    @Override
    protected boolean shouldNotFilter(@NonNull final HttpServletRequest request) {
        final String path = request.getRequestURI();
        return path.startsWith("/actuator/health")
                || path.startsWith("/actuator/info")
                || path.startsWith("/favicon.ico")
                || path.startsWith("/error");
    }
}
