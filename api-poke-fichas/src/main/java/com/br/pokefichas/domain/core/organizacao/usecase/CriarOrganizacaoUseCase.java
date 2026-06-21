package com.br.pokefichas.domain.core.organizacao.usecase;

import com.br.pokefichas.domain.core.organizacao.dto.CriarOrganizacaoRequest;
import com.br.pokefichas.domain.core.organizacao.dto.OrganizacaoResponse;
import com.br.pokefichas.domain.core.organizacao.model.Organizacao;
import com.br.pokefichas.domain.core.organizacao.model.OrganizacaoMapper;
import com.br.pokefichas.domain.core.organizacao.repository.OrganizacaoCommand;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CriarOrganizacaoUseCase {

    private final OrganizacaoCommand command;
    private final OrganizacaoMapper mapper;

    public CriarOrganizacaoUseCase(final OrganizacaoCommand command, final OrganizacaoMapper mapper) {
        this.command = command;
        this.mapper = mapper;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public OrganizacaoResponse handle(final CriarOrganizacaoRequest request) {
        final Organizacao saved = command.save(mapper.toEntity(request));
        return mapper.toResponse(saved);
    }
}
