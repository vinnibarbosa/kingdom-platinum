package com.br.pokefichas.domain.core.usuario.usecase;

import com.br.pokefichas.commons.exception.EntityNotFoundException;
import com.br.pokefichas.domain.core.usuario.dto.AtualizarUsuarioRequest;
import com.br.pokefichas.domain.core.usuario.dto.UsuarioResponse;
import com.br.pokefichas.domain.core.usuario.model.Usuario;
import com.br.pokefichas.domain.core.usuario.model.UsuarioMapper;
import com.br.pokefichas.domain.core.usuario.repository.UsuarioCommand;
import com.br.pokefichas.domain.core.usuario.repository.UsuarioQuery;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AtualizarUsuarioUseCase {

    private final UsuarioCommand command;
    private final UsuarioQuery query;
    private final UsuarioMapper mapper;

    public AtualizarUsuarioUseCase(final UsuarioCommand command,
                                   final UsuarioQuery query,
                                   final UsuarioMapper mapper) {
        this.command = command;
        this.query = query;
        this.mapper = mapper;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public UsuarioResponse handle(final Long id, final AtualizarUsuarioRequest request) {
        final Usuario usuario = query.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario", id));

        final Usuario updatedUsuario = command.save(mapper.toEntity(usuario, request));
        return mapper.toResponse(updatedUsuario);
    }
}
