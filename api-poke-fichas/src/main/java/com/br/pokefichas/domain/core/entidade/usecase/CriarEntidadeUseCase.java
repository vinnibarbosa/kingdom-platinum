package com.br.pokefichas.domain.core.entidade.usecase;

import com.br.pokefichas.commons.organizacao.OrganizacaoContext;
import com.br.pokefichas.domain.core.entidade.dto.CriarEntidadeRequest;
import com.br.pokefichas.domain.core.entidade.dto.EntidadeResponse;
import com.br.pokefichas.domain.core.entidade.model.Entidade;
import com.br.pokefichas.domain.core.entidade.model.EntidadeMapper;
import com.br.pokefichas.domain.core.entidade.repository.EntidadeCommand;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CriarEntidadeUseCase {

    private final EntidadeCommand command;
    private final EntidadeMapper mapper;
    private final OrganizacaoContext organizacaoContext;

    public CriarEntidadeUseCase(final EntidadeCommand command,
                                final EntidadeMapper mapper,
                                final OrganizacaoContext organizacaoContext) {
        this.command = command;
        this.mapper = mapper;
        this.organizacaoContext = organizacaoContext;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public EntidadeResponse handle(final CriarEntidadeRequest request) {
        final Long idOrganizacao = organizacaoContext.getRequiredOrganizacaoId();
        final Entidade saved = command.save(mapper.toEntity(request, idOrganizacao));
        return mapper.toResponse(saved);
    }
}
