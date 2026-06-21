package com.br.pokefichas.domain.core.organizacao.usecase;

import com.br.pokefichas.commons.exception.EntityNotFoundException;
import com.br.pokefichas.domain.core.organizacao.dto.OrganizacaoResponse;
import com.br.pokefichas.domain.core.organizacao.model.OrganizacaoMapper;
import com.br.pokefichas.domain.core.organizacao.repository.OrganizacaoQuery;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class BuscarOrganizacaoUseCase {

    private final OrganizacaoQuery query;
    private final OrganizacaoMapper mapper;

    public BuscarOrganizacaoUseCase(final OrganizacaoQuery query, final OrganizacaoMapper mapper) {
        this.query = query;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public OrganizacaoResponse handle(final Long id) {
        return query.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Organizacao", id));
    }
}
