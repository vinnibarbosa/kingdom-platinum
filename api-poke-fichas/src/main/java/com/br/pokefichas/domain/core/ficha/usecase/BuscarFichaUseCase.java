package com.br.pokefichas.domain.core.ficha.usecase;

import com.br.pokefichas.commons.exception.EntityNotFoundException;
import com.br.pokefichas.domain.core.ficha.dto.FichaResponse;
import com.br.pokefichas.domain.core.ficha.model.Ficha;
import com.br.pokefichas.domain.core.ficha.model.FichaDetalhes;
import com.br.pokefichas.domain.core.ficha.model.FichaMapper;
import com.br.pokefichas.domain.core.ficha.repository.FichaQuery;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class BuscarFichaUseCase {

    private final FichaQuery query;
    private final FichaMapper mapper;

    public BuscarFichaUseCase(final FichaQuery query, final FichaMapper mapper) {
        this.query = query;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public FichaResponse handle(final Long id) {
        final Ficha ficha = query.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ficha nao encontrada: " + id));
        final FichaDetalhes detalhes = query.findDetalhes(id);
        return mapper.toResponse(ficha, detalhes);
    }

    @Transactional(readOnly = true)
    public FichaResponse handlePublico(final Long id) {
        final Ficha ficha = query.findByIdWithoutContext(id)
                .orElseThrow(() -> new EntityNotFoundException("Ficha nao encontrada: " + id));
        final FichaDetalhes detalhes = query.findDetalhesWithoutContext(id);
        return mapper.toResponse(ficha, detalhes);
    }

    @Transactional(readOnly = true)
    public FichaResponse handleAdmin(final Long id) {
        final Ficha ficha = query.findByIdWithoutContext(id)
                .orElseThrow(() -> new EntityNotFoundException("Ficha nao encontrada: " + id));
        final FichaDetalhes detalhes = query.findDetalhesWithoutContext(id);
        return mapper.toResponse(ficha, detalhes);
    }
}
