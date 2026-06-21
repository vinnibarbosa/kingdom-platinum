package com.br.pokefichas.domain.core.organizacao.model;

import com.br.pokefichas.commons.utils.ObjectUtil;
import com.br.pokefichas.domain.core.organizacao.dto.AtualizarOrganizacaoRequest;
import com.br.pokefichas.domain.core.organizacao.dto.CriarOrganizacaoRequest;
import com.br.pokefichas.domain.core.organizacao.dto.OrganizacaoResponse;
import org.springframework.stereotype.Component;

@Component
public class OrganizacaoMapper {

    public Organizacao toEntity(final CriarOrganizacaoRequest request) {
        return Organizacao.Builder.create()
                .nome(request.nome())
                .ativo(ObjectUtil.getIfExists(request.ativo(), ativo -> ativo, true))
                .build();
    }

    public Organizacao toEntity(final Organizacao organizacao, final AtualizarOrganizacaoRequest request) {
        return Organizacao.Builder.from(organizacao)
                .nome(request.nome())
                .ativo(ObjectUtil.getIfExists(request.ativo(), ativo -> ativo, true))
                .build();
    }

    public OrganizacaoResponse toResponse(final Organizacao organizacao) {
        return new OrganizacaoResponse(
                organizacao.getId(),
                organizacao.getNome(),
                organizacao.isAtivo(),
                organizacao.getCreatedAt(),
                organizacao.getCreatedBy(),
                organizacao.getUpdatedAt(),
                organizacao.getUpdatedBy()
        );
    }
}
