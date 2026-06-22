package com.br.pokefichas.domain.core.usuario.usecase;

import com.br.pokefichas.commons.exception.EntityNotFoundException;
import com.br.pokefichas.commons.exception.BusinessException;
import com.br.pokefichas.commons.exception.UnauthorizedException;
import com.br.pokefichas.commons.security.service.RefreshTokenService;
import com.br.pokefichas.commons.useraccess.UserAccess;
import com.br.pokefichas.domain.core.usuario.dto.RedefinirSenhaRequest;
import com.br.pokefichas.domain.core.usuario.model.Usuario;
import com.br.pokefichas.domain.core.usuario.repository.UsuarioCommand;
import com.br.pokefichas.domain.core.usuario.repository.UsuarioQuery;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RedefinirSenhaAdminUseCase {

    private final UsuarioCommand command;
    private final UsuarioQuery query;
    private final UserAccess userAccess;
    private final RefreshTokenService refreshTokenService;

    public RedefinirSenhaAdminUseCase(final UsuarioCommand command,
                                     final UsuarioQuery query,
                                     final UserAccess userAccess,
                                     final RefreshTokenService refreshTokenService) {
        this.command = command;
        this.query = query;
        this.userAccess = userAccess;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    public void handle(final RedefinirSenhaRequest request) {
        final boolean admin = userAccess.getRole().map("ADMIN"::equals).orElse(false);
        if (!admin) {
            throw new UnauthorizedException("Apenas administradores podem redefinir senhas de outras contas");
        }

        final String currentUsername = Usuario.normalizeUsername(userAccess.getUsername().orElse(null));
        final String targetUsername = Usuario.normalizeUsername(request.username());
        if (targetUsername != null && targetUsername.equals(currentUsername)) {
            throw new BusinessException("Informe outra conta para redefinir a senha");
        }

        final Usuario usuario = query.findByUsernameWithoutTenant(targetUsername)
                .orElseThrow(() -> new EntityNotFoundException("Usuario nao encontrado"));

        command.markPasswordResetPendingWithoutTenant(usuario.getId(), usuario.getAuthVersion() + 1);
        refreshTokenService.revokeAllUserTokens(usuario);
    }
}
