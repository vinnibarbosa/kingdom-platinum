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
import com.br.pokefichas.domain.core.loja.model.LojaCupom;
import com.br.pokefichas.domain.core.loja.model.LojaItem;
import com.br.pokefichas.domain.core.loja.repository.LojaCupomQuery;
import com.br.pokefichas.domain.core.loja.repository.LojaItemQuery;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class ComprarItemLojaUseCase {
    private final LojaItemQuery lojaQuery;
    private final LojaCupomQuery cupomQuery;
    private final FichaQuery fichaQuery;
    private final FichaCommand fichaCommand;
    private final UserAccess userAccess;
    private final FichaHistoricoWriter historicoWriter;

    public ComprarItemLojaUseCase(final LojaItemQuery lojaQuery, final LojaCupomQuery cupomQuery, final FichaQuery fichaQuery,
                                  final FichaCommand fichaCommand, final UserAccess userAccess,
                                  final FichaHistoricoWriter historicoWriter) {
        this.lojaQuery = lojaQuery; this.cupomQuery = cupomQuery;
        this.fichaQuery = fichaQuery;
        this.fichaCommand = fichaCommand;
        this.userAccess = userAccess;
        this.historicoWriter = historicoWriter;
    }

    @Transactional
    public CompraLojaResponse handle(final CompraLojaRequest request) {
        final Long idUsuario = userAccess.getId().orElseThrow(() -> new BusinessException("Usuario atual nao identificado."));
        final Ficha ficha = fichaQuery.findById(request.idFicha())
                .orElseThrow(() -> new EntityNotFoundException("Ficha nao encontrada."));

        if (ficha.isNpc() || !idUsuario.equals(ficha.getIdUsuario())) {
            throw new BusinessException("A ficha escolhida nao pertence a sua conta.", "FICHA_NOT_OWNED");
        }

        final Map<Long, Integer> quantities = new LinkedHashMap<>();
        request.itens().forEach(item -> quantities.merge(item.idItem(), item.quantidade(), Integer::sum));
        final List<LineItem> lines = quantities.entrySet().stream().map(entry -> {
            final LojaItem item = lojaQuery.findById(entry.getKey()).filter(LojaItem::isAtivo)
                    .orElseThrow(() -> new EntityNotFoundException("Item da loja nao encontrado."));
            if (item.getPreco() == null || item.getPreco().signum() <= 0) {
                throw new BusinessException("Este item ainda não possui um preço definido.", "STORE_ITEM_UNPRICED");
            }
            return new LineItem(item, entry.getValue());
        }).toList();
        final BigDecimal subtotal = lines.stream().map(LineItem::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        final LojaCupom cupom = resolveCoupon(request.cupom());
        final BigDecimal desconto = cupom == null ? BigDecimal.ZERO : subtotal
                .multiply(BigDecimal.valueOf(cupom.getPercentual()))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        final BigDecimal total = subtotal.subtract(desconto);
        final BigDecimal saldo = ficha.getDinheiro() == null ? BigDecimal.ZERO : ficha.getDinheiro();
        if (saldo.compareTo(total) < 0) {
            throw new BusinessException("Dinheiro insuficiente para concluir esta compra.", "INSUFFICIENT_FUNDS");
        }

        final Ficha fichaAtualizada = fichaCommand.save(Ficha.Builder.from(ficha)
                .dinheiro(saldo.subtract(total))
                .build());

        final List<FichaItem> items = new ArrayList<>();
        int order = nextItemOrder(ficha);
        for (final LineItem line : lines) {
            final LojaItem itemLoja = line.item();
            final int itemOrder = order++;
            items.add(fichaQuery.findItemByFichaAndCodigo(ficha.getId(), codigoInventario(itemLoja))
                    .map(existente -> FichaItem.Builder.from(existente).quantidade(existente.getQuantidade() + line.quantity()).build())
                    .orElseGet(() -> FichaItem.Builder.create()
                            .idOrganizacao(ficha.getIdOrganizacao()).idFicha(ficha.getId()).categoria(itemLoja.getCategoria())
                            .codigo(codigoInventario(itemLoja)).icone(itemLoja.getIcone()).nome(itemLoja.getNome())
                            .descricao(itemLoja.getDescricao()).quantidade(line.quantity()).ordem(itemOrder).build()));
        }
        fichaCommand.saveItens(items);
        final List<String> historyItems = lines.stream().map(line -> line.quantity() + "x " + line.item().getNome()).toList();
        historicoWriter.recordStorePurchase(fichaAtualizada.getId(), fichaAtualizada.getIdOrganizacao(), historyItems, total,
                cupom == null ? null : cupom.getCodigo());

        return new CompraLojaResponse(
                fichaAtualizada.getId(),
                fichaAtualizada.getDinheiro(),
                historyItems,
                subtotal,
                desconto,
                total
        );
    }

    private LojaCupom resolveCoupon(final String requestedCoupon) {
        if (requestedCoupon == null || requestedCoupon.isBlank()) return null;
        final String code = requestedCoupon.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
        return cupomQuery.findAtivoByCodigo(code)
                .orElseThrow(() -> new BusinessException("Cupom inválido ou indisponível.", "INVALID_COUPON"));
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

    private record LineItem(LojaItem item, int quantity) {
        BigDecimal subtotal() { return item.getPreco().multiply(BigDecimal.valueOf(quantity)); }
    }
}
