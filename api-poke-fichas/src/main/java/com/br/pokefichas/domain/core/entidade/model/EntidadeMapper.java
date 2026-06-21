package com.br.pokefichas.domain.core.entidade.model;

import com.br.pokefichas.commons.utils.ObjectUtil;
import com.br.pokefichas.domain.core.entidade.dto.AtualizarEntidadeRequest;
import com.br.pokefichas.domain.core.entidade.dto.CriarEntidadeRequest;
import com.br.pokefichas.domain.core.entidade.dto.EntidadeResponse;
import org.springframework.stereotype.Component;

@Component
public class EntidadeMapper {

    public Entidade toEntity(final CriarEntidadeRequest request, final Long idOrganizacao) {
        return Entidade.Builder.create()
                .idOrganizacao(idOrganizacao)
                .nome(request.nome())
                .nomeFantasia(request.nomeFantasia())
                .inscricaoEstadual(request.inscricaoEstadual())
                .telefone(request.telefone())
                .ativo(ObjectUtil.getIfExists(request.ativo(), ativo -> ativo, true))
                .build();
    }

    public Entidade toEntity(final Entidade entidade,
                             final AtualizarEntidadeRequest request,
                             final Long idOrganizacao) {
        return Entidade.Builder.from(entidade)
                .idOrganizacao(idOrganizacao)
                .nome(request.nome())
                .nomeFantasia(request.nomeFantasia())
                .inscricaoEstadual(request.inscricaoEstadual())
                .telefone(request.telefone())
                .ativo(ObjectUtil.getIfExists(request.ativo(), ativo -> ativo, true))
                .build();
    }

    public EntidadeResponse toResponse(final Entidade entidade) {
        return new EntidadeResponse(
                entidade.getId(),
                entidade.getIdOrganizacao(),
                entidade.getNome(),
                entidade.getNomeFantasia(),
                entidade.getInscricaoEstadual(),
                entidade.getTelefone(),
                entidade.isAtivo(),
                entidade.getCreatedAt(),
                entidade.getCreatedBy(),
                entidade.getUpdatedAt(),
                entidade.getUpdatedBy()
        );
    }
}
