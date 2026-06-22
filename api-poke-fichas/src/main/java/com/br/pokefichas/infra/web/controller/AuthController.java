package com.br.pokefichas.infra.web.controller;

import com.br.pokefichas.commons.exception.AuthenticationException;
import com.br.pokefichas.commons.security.dto.AuthRequest;
import com.br.pokefichas.commons.security.dto.AuthResponse;
import com.br.pokefichas.commons.security.dto.RefreshTokenRequest;
import com.br.pokefichas.commons.security.usecase.LoginUseCase;
import com.br.pokefichas.commons.security.usecase.LogoutUseCase;
import com.br.pokefichas.commons.security.usecase.RefreshTokenUseCase;
import com.br.pokefichas.commons.security.util.CookieUtil;
import com.br.pokefichas.domain.core.usuario.dto.RegistrarContaRequest;
import com.br.pokefichas.domain.core.usuario.usecase.RegistrarUsuarioPublicoUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticacao", description = "Endpoints de autenticacao")
public class AuthController {

    private final LoginUseCase login;
    private final RefreshTokenUseCase refreshToken;
    private final LogoutUseCase logout;
    private final CookieUtil cookieUtil;
    private final RegistrarUsuarioPublicoUseCase registrar;

    public AuthController(final LoginUseCase login,
                          final RefreshTokenUseCase refreshToken,
                          final LogoutUseCase logout,
                          final CookieUtil cookieUtil,
                          final RegistrarUsuarioPublicoUseCase registrar) {
        this.login = login;
        this.refreshToken = refreshToken;
        this.logout = logout;
        this.cookieUtil = cookieUtil;
        this.registrar = registrar;
    }

    @PostMapping("/registrar")
    @Operation(summary = "Criar conta pública")
    public ResponseEntity<Void> registrar(@Valid @RequestBody final RegistrarContaRequest request) {
        registrar.handle(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    @Operation(summary = "Login de usuario")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody final AuthRequest request,
            final HttpServletResponse httpResponse) {

        final AuthResponse response = login.handle(request);
        cookieUtil.addCookie(httpResponse, cookieUtil.createRefreshTokenCookie(response.refreshToken()));

        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renovar access token")
    public ResponseEntity<AuthResponse> refresh(
            final HttpServletRequest httpRequest,
            final HttpServletResponse httpResponse) {

        final String refreshTokenValue = cookieUtil
                .getCookieValue(httpRequest, CookieUtil.REFRESH_TOKEN_COOKIE)
                .orElseThrow(AuthenticationException::invalidToken);

        final AuthResponse response = refreshToken.handle(new RefreshTokenRequest(refreshTokenValue));
        cookieUtil.addCookie(httpResponse, cookieUtil.createRefreshTokenCookie(response.refreshToken()));

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout de usuario")
    public ResponseEntity<Void> logout(final HttpServletResponse httpResponse) {
        logout.handle();
        cookieUtil.addCookie(httpResponse, cookieUtil.deleteRefreshTokenCookie());
        return ResponseEntity.ok().build();
    }

}
