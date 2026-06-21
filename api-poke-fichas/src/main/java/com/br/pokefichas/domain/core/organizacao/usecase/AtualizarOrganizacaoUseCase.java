package com.br.pokefichas.domain.core.organizacao.usecase;

import com.br.pokefichas.commons.exception.EntityNotFoundException;
import com.br.pokefichas.domain.core.organizacao.dto.AtualizarOrganizacaoRequest;
import com.br.pokefichas.domain.core.organizacao.dto.OrganizacaoResponse;
import com.br.pokefichas.domain.core.organizacao.model.Organizacao;
import com.br.pokefichas.domain.core.organizacao.model.OrganizacaoMapper;
import com.br.pokefichas.domain.core.organizacao.repository.OrganizacaoCommand;
import com.br.pokefichas.domain.core.organizacao.repository.OrganizacaoQuery;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AtualizarOrganizacaoUseCase {

    private final OrganizacaoQuery query;
    private final OrganizacaoCommand command;
    private final OrganizacaoMapper mapper;

    public AtualizarOrganizacaoUseCase(final OrganizacaoQuery query,
                                       final OrganizacaoCommand command,
                                       final OrganizacaoMapper mapper) {
        this.query = query;
        this.command = command;
        this.mapper = mapper;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public OrganizacaoResponse handle(final Long id, final AtualizarOrganizacaoRequest request) {
        final Organizacao organizacao = query.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Organizacao", id));

        final Organizacao updated = command.save(mapper.toEntity(organizacao, request));
        return mapper.toResponse(updated);
    }
}
