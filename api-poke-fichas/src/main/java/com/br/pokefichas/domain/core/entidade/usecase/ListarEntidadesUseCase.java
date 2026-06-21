package com.br.pokefichas.domain.core.entidade.usecase;

import com.br.pokefichas.commons.dto.PageRequest;
import com.br.pokefichas.commons.page.Page;
import com.br.pokefichas.domain.core.entidade.dto.EntidadeResponse;
import com.br.pokefichas.domain.core.entidade.model.Entidade;
import com.br.pokefichas.domain.core.entidade.model.EntidadeMapper;
import com.br.pokefichas.domain.core.entidade.repository.EntidadeQuery;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ListarEntidadesUseCase {

    private final EntidadeQuery query;
    private final EntidadeMapper mapper;

    public ListarEntidadesUseCase(final EntidadeQuery query, final EntidadeMapper mapper) {
        this.query = query;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public Page<EntidadeResponse> handle(final PageRequest pageRequest) {
        final Page<Entidade> entidades = query.findAll(pageRequest);
        return entidades.map(mapper::toResponse);
    }
}
