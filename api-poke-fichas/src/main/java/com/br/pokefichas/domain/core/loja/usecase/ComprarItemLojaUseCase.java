package com.br.pokefichas.domain.core.loja.usecase;

import com.br.pokefichas.commons.exception.BusinessException;
import com.br.pokefichas.commons.exception.EntityNotFoundException;
import com.br.pokefichas.commons.useraccess.UserAccess;
import com.br.pokefichas.domain.core.ficha.dto.FichaItemResponse;
import com.br.pokefichas.domain.core.ficha.model.Ficha;
import com.br.pokefichas.domain.core.ficha.model.FichaItem;
import com.br.pokefichas.domain.core.ficha.repository.FichaCommand;
import com.br.pokefichas.domain.core.ficha.repository.FichaQuery;
import com.br.pokefichas.domain.core.ficha.usecase.FichaHistoricoWriter;
import com.br.pokefichas.domain.core.loja.dto.CompraLojaRequest;
import com.br.pokefichas.domain.core.loja.dto.CompraLojaResponse;
import com.br.pokefichas.domain.core.loja.model.LojaItem;
import com.br.pokefichas.domain.core.loja.repository.LojaItemQuery;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
public class ComprarItemLojaUseCase {
    private final LojaItemQuery lojaQuery;
    private final FichaQuery fichaQuery;
    private final FichaCommand fichaCommand;
    private final UserAccess userAccess;
    private final FichaHistoricoWriter historicoWriter;

    public ComprarItemLojaUseCase(final LojaItemQuery lojaQuery, final FichaQuery fichaQuery,
                                  final FichaCommand fichaCommand, final UserAccess userAccess,
                                  final FichaHistoricoWriter historicoWriter) {
        this.lojaQuery = lojaQuery;
        this.fichaQuery = fichaQuery;
        this.fichaCommand = fichaCommand;
        this.userAccess = userAccess;
        this.historicoWriter = historicoWriter;
    }

    @Transactional
    public CompraLojaResponse handle(final CompraLojaRequest request) {
        final Long idUsuario = userAccess.getId().orElseThrow(() -> new BusinessException("Usuario atual nao identificado."));
        final LojaItem itemLoja = lojaQuery.findById(request.idItem())
                .filter(LojaItem::isAtivo)
                .orElseThrow(() -> new EntityNotFoundException("Item da loja nao encontrado."));
        final Ficha ficha = fichaQuery.findById(request.idFicha())
                .orElseThrow(() -> new EntityNotFoundException("Ficha nao encontrada."));

        if (ficha.isNpc() || !idUsuario.equals(ficha.getIdUsuario())) {
            throw new BusinessException("A ficha escolhida nao pertence a sua conta.", "FICHA_NOT_OWNED");
        }

        final BigDecimal total = itemLoja.getPreco().multiply(BigDecimal.valueOf(request.quantidade()));
        final BigDecimal saldo = ficha.getDinheiro() == null ? BigDecimal.ZERO : ficha.getDinheiro();
        if (saldo.compareTo(total) < 0) {
            throw new BusinessException("Dinheiro insuficiente para concluir esta compra.", "INSUFFICIENT_FUNDS");
        }

        final Ficha fichaAtualizada = fichaCommand.save(Ficha.Builder.from(ficha)
                .dinheiro(saldo.subtract(total))
                .build());

        final FichaItem itemInventario = fichaQuery.findItemByFichaAndCodigo(ficha.getId(), codigoInventario(itemLoja))
                .map(existente -> FichaItem.Builder.from(existente)
                        .quantidade(existente.getQuantidade() + request.quantidade())
                        .build())
                .orElseGet(() -> FichaItem.Builder.create()
                        .idOrganizacao(ficha.getIdOrganizacao())
                        .idFicha(ficha.getId())
                        .categoria(itemLoja.getCategoria())
                        .codigo(codigoInventario(itemLoja))
                        .icone(itemLoja.getIcone())
                        .nome(itemLoja.getNome())
                        .descricao(itemLoja.getDescricao())
                        .quantidade(request.quantidade())
                        .ordem(nextItemOrder(ficha))
                        .build());
        final FichaItem savedItem = fichaCommand.saveItens(java.util.List.of(itemInventario)).getFirst();
        historicoWriter.recordStorePurchase(
                fichaAtualizada.getId(),
                fichaAtualizada.getIdOrganizacao(),
                itemLoja.getNome(),
                request.quantidade(),
                total
        );

        return new CompraLojaResponse(
                fichaAtualizada.getId(),
                fichaAtualizada.getDinheiro(),
                toResponse(savedItem),
                total
        );
    }

    private String codigoInventario(final LojaItem item) {
        return item.getCodigo() == null || item.getCodigo().isBlank() ? "loja-" + item.getId() : item.getCodigo();
    }

    private int nextItemOrder(final Ficha ficha) {
        return fichaQuery.findDetalhes(ficha.getId()).itens().stream()
                .map(FichaItem::getOrdem)
                .filter(java.util.Objects::nonNull)
                .max(Integer::compareTo)
                .map(value -> value + 1)
                .orElse(0);
    }

    private FichaItemResponse toResponse(final FichaItem item) {
        return new FichaItemResponse(item.getId(), item.getCategoria(), item.getCodigo(), item.getIcone(),
                item.getNome(), item.getQuantidade(), item.getDescricao(), item.getOrdem());
    }
}
