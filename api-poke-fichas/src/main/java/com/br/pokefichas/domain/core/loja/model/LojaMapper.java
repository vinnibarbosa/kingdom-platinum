package com.br.pokefichas.domain.core.loja.model;

import com.br.pokefichas.domain.core.loja.dto.LojaItemRequest;
import com.br.pokefichas.domain.core.loja.dto.LojaItemResponse;
import org.springframework.stereotype.Component;

@Component
public class LojaMapper {

    public LojaItem toEntity(final LojaItemRequest request, final Long idOrganizacao) {
        return apply(LojaItem.Builder.create().idOrganizacao(idOrganizacao), request).build();
    }

    public LojaItem toEntity(final LojaItem item, final LojaItemRequest request) {
        return apply(LojaItem.Builder.from(item), request).build();
    }

    public LojaItemResponse toResponse(final LojaItem item) {
        return new LojaItemResponse(
                item.getId(), item.getCategoria(), item.getCodigo(), item.getIcone(), item.getNome(),
                item.getDescricao(), item.getPreco(), item.isAtivo(), item.getOrdem()
        );
    }

    private LojaItem.Builder apply(final LojaItem.Builder builder, final LojaItemRequest request) {
        return builder
                .categoria(request.categoria().trim())
                .codigo(blankToNull(request.codigo()))
                .icone(blankToNull(request.icone()))
                .nome(request.nome().trim())
                .descricao(blankToNull(request.descricao()))
                .preco(request.preco())
                .ativo(request.ativo() == null || request.ativo())
                .ordem(request.ordem() == null ? 0 : request.ordem());
    }

    private String blankToNull(final String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
