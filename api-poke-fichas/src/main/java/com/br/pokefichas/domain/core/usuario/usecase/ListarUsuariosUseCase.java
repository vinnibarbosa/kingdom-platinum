package com.br.pokefichas.domain.core.usuario.usecase;

import com.br.pokefichas.commons.dto.PageRequest;
import com.br.pokefichas.commons.page.Page;
import com.br.pokefichas.domain.core.usuario.dto.UsuarioResponse;
import com.br.pokefichas.domain.core.usuario.model.Usuario;
import com.br.pokefichas.domain.core.usuario.model.UsuarioMapper;
import com.br.pokefichas.domain.core.usuario.repository.UsuarioQuery;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ListarUsuariosUseCase {

    private final UsuarioQuery query;
    private final UsuarioMapper mapper;

    public ListarUsuariosUseCase(final UsuarioQuery query, final UsuarioMapper mapper) {
        this.query = query;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public Page<UsuarioResponse> handle(final PageRequest pageRequest) {
        final Page<Usuario> usuarios = query.findAll(pageRequest);
        return usuarios.map(mapper::toResponse);
    }
}
