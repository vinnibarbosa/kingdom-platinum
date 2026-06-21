package com.br.pokefichas.domain.core.organizacao.usecase;

import com.br.pokefichas.commons.dto.PageRequest;
import com.br.pokefichas.commons.page.Page;
import com.br.pokefichas.domain.core.organizacao.dto.OrganizacaoResponse;
import com.br.pokefichas.domain.core.organizacao.model.Organizacao;
import com.br.pokefichas.domain.core.organizacao.model.OrganizacaoMapper;
import com.br.pokefichas.domain.core.organizacao.repository.OrganizacaoQuery;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ListarOrganizacoesUseCase {

    private final OrganizacaoQuery query;
    private final OrganizacaoMapper mapper;

    public ListarOrganizacoesUseCase(final OrganizacaoQuery query, final OrganizacaoMapper mapper) {
        this.query = query;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public Page<OrganizacaoResponse> handle(final PageRequest pageRequest) {
        final Page<Organizacao> organizacoes = query.findAll(pageRequest);
        return organizacoes.map(mapper::toResponse);
    }
}
