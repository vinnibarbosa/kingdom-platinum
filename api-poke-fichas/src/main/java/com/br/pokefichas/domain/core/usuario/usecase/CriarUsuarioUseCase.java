package com.br.pokefichas.domain.core.usuario.usecase;

import com.br.pokefichas.domain.core.usuario.dto.CriarUsuarioRequest;
import com.br.pokefichas.domain.core.usuario.dto.UsuarioResponse;
import com.br.pokefichas.domain.core.usuario.model.Usuario;
import com.br.pokefichas.domain.core.usuario.model.UsuarioMapper;
import com.br.pokefichas.domain.core.usuario.repository.UsuarioCommand;
import com.br.pokefichas.commons.tenant.TenantContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CriarUsuarioUseCase {

    private final UsuarioCommand command;
    private final UsuarioMapper mapper;
    private final TenantContext tenantContext;

    public CriarUsuarioUseCase(final UsuarioCommand command,
                               final UsuarioMapper mapper,
                               final TenantContext tenantContext) {
        this.command = command;
        this.mapper = mapper;
        this.tenantContext = tenantContext;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public UsuarioResponse handle(final CriarUsuarioRequest request) {
        final Usuario savedUsuario = command.save(mapper.toEntity(request, tenantContext.getRequiredTenantId()));
        return mapper.toResponse(savedUsuario);
    }
}
