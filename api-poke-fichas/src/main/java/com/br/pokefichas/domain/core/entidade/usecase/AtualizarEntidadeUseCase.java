package com.br.pokefichas.domain.core.entidade.usecase;

import com.br.pokefichas.commons.exception.EntityNotFoundException;
import com.br.pokefichas.commons.organizacao.OrganizacaoContext;
import com.br.pokefichas.domain.core.entidade.dto.AtualizarEntidadeRequest;
import com.br.pokefichas.domain.core.entidade.dto.EntidadeResponse;
import com.br.pokefichas.domain.core.entidade.model.Entidade;
import com.br.pokefichas.domain.core.entidade.model.EntidadeMapper;
import com.br.pokefichas.domain.core.entidade.repository.EntidadeCommand;
import com.br.pokefichas.domain.core.entidade.repository.EntidadeQuery;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AtualizarEntidadeUseCase {

    private final EntidadeQuery query;
    private final EntidadeCommand command;
    private final EntidadeMapper mapper;
    private final OrganizacaoContext organizacaoContext;

    public AtualizarEntidadeUseCase(final EntidadeQuery query,
                                    final EntidadeCommand command,
                                    final EntidadeMapper mapper,
                                    final OrganizacaoContext organizacaoContext) {
        this.query = query;
        this.command = command;
        this.mapper = mapper;
        this.organizacaoContext = organizacaoContext;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public EntidadeResponse handle(final Long id, final AtualizarEntidadeRequest request) {
        final Entidade entidade = query.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Entidade", id));

        final Long idOrganizacao = organizacaoContext.getRequiredOrganizacaoId();
        final Entidade updated = command.save(mapper.toEntity(entidade, request, idOrganizacao));
        return mapper.toResponse(updated);
    }
}
