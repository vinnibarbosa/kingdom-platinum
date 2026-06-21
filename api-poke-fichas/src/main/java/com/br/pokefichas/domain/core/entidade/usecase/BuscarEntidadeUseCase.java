package com.br.pokefichas.domain.core.entidade.usecase;

import com.br.pokefichas.commons.exception.EntityNotFoundException;
import com.br.pokefichas.domain.core.entidade.dto.EntidadeResponse;
import com.br.pokefichas.domain.core.entidade.model.EntidadeMapper;
import com.br.pokefichas.domain.core.entidade.repository.EntidadeQuery;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class BuscarEntidadeUseCase {

    private final EntidadeQuery query;
    private final EntidadeMapper mapper;

    public BuscarEntidadeUseCase(final EntidadeQuery query, final EntidadeMapper mapper) {
        this.query = query;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public EntidadeResponse handle(final Long id) {
        return query.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Entidade", id));
    }
}
