package com.br.pokefichas.domain.core.usuario.usecase;

import com.br.pokefichas.commons.exception.UnauthorizedException;
import com.br.pokefichas.commons.exception.BusinessException;
import com.br.pokefichas.commons.security.service.RefreshTokenService;
import com.br.pokefichas.commons.useraccess.UserAccess;
import com.br.pokefichas.domain.core.usuario.dto.RedefinirSenhaRequest;
import com.br.pokefichas.domain.core.usuario.model.Usuario;
import com.br.pokefichas.domain.core.usuario.repository.UsuarioCommand;
import com.br.pokefichas.domain.core.usuario.repository.UsuarioQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedefinirSenhaAdminUseCaseTest {

    @Mock
    private UsuarioCommand command;

    @Mock
    private UsuarioQuery query;

    @Mock
    private UserAccess userAccess;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private RedefinirSenhaAdminUseCase useCase;

    @Test
    void shouldRejectNonAdminUser() {
        when(userAccess.getRole()).thenReturn(Optional.of("OPERADOR"));

        assertThatThrownBy(() -> useCase.handle(new RedefinirSenhaRequest("jogador")))
                .isInstanceOf(UnauthorizedException.class);

        verifyNoInteractions(command, query, refreshTokenService);
    }

    @Test
    void shouldResetPasswordAndRevokeSessionsForAdmin() {
        final Usuario usuario = mock(Usuario.class);
        when(usuario.getId()).thenReturn(42L);
        when(usuario.getAuthVersion()).thenReturn(2);
        when(userAccess.getRole()).thenReturn(Optional.of("ADMIN"));
        when(userAccess.getUsername()).thenReturn(Optional.of("vinni"));
        when(query.findByUsernameWithoutTenant("jogador")).thenReturn(Optional.of(usuario));

        useCase.handle(new RedefinirSenhaRequest("jogador"));

        verify(command).markPasswordResetPendingWithoutTenant(42L, 3);
        verify(refreshTokenService).revokeAllUserTokens(usuario);
    }

    @Test
    void shouldRejectResetForOwnAdminAccount() {
        when(userAccess.getRole()).thenReturn(Optional.of("ADMIN"));
        when(userAccess.getUsername()).thenReturn(Optional.of("vinni"));

        assertThatThrownBy(() -> useCase.handle(new RedefinirSenhaRequest(" Vinni ")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Informe outra conta para redefinir a senha");

        verifyNoInteractions(command, query, refreshTokenService);
    }
}
