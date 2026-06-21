package com.br.pokefichas.domain.core.usuario.usecase;

import com.br.pokefichas.commons.exception.EntityNotFoundException;
import com.br.pokefichas.domain.core.usuario.dto.UsuarioResponse;
import com.br.pokefichas.domain.core.usuario.model.Usuario;
import com.br.pokefichas.domain.core.usuario.model.UsuarioMapper;
import com.br.pokefichas.domain.core.usuario.repository.UsuarioQuery;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class BuscarUsuarioUseCase {

    private final UsuarioQuery query;
    private final UsuarioMapper mapper;

    public BuscarUsuarioUseCase(final UsuarioQuery query, final UsuarioMapper mapper) {
        this.query = query;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public UsuarioResponse handle(final Long id) {
        final Usuario usuario = query.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario", id));
        return mapper.toResponse(usuario);
    }
}
