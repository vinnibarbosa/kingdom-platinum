package com.br.pokefichas.domain.core.integracao.usecase;

import com.br.pokefichas.commons.exception.BusinessException;
import com.br.pokefichas.commons.useraccess.UserAccess;
import com.br.pokefichas.domain.core.ficha.model.Ficha;
import com.br.pokefichas.domain.core.ficha.model.FichaItem;
import com.br.pokefichas.domain.core.ficha.repository.FichaCommand;
import com.br.pokefichas.domain.core.ficha.repository.FichaQuery;
import com.br.pokefichas.domain.core.ficha.usecase.FichaHistoricoWriter;
import com.br.pokefichas.domain.core.integracao.dto.ConsumirHoneyRequest;
import com.br.pokefichas.domain.core.integracao.dto.ConsumirHoneyResponse;
import com.br.pokefichas.domain.core.integracao.dto.FichaRolagemResponse;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Component
public class IntegrarRolagemUseCase {

    private static final int HONEY_BONUS = 2;

    private final FichaQuery fichaQuery;
    private final FichaCommand fichaCommand;
    private final FichaHistoricoWriter historicoWriter;
    private final UserAccess userAccess;

    public IntegrarRolagemUseCase(final FichaQuery fichaQuery,
                                  final FichaCommand fichaCommand,
                                  final FichaHistoricoWriter historicoWriter,
                                  final UserAccess userAccess) {
        this.fichaQuery = fichaQuery;
        this.fichaCommand = fichaCommand;
        this.historicoWriter = historicoWriter;
        this.userAccess = userAccess;
    }

    @Transactional(readOnly = true)
    public List<FichaRolagemResponse> listarFichas() {
        final Long idUsuario = currentUserId();
        return fichaQuery.findByUsuario(idUsuario).stream()
                .map(ficha -> new FichaRolagemResponse(
                        ficha.getId(),
                        ficha.getNome(),
                        ficha.getRanking(),
                        honeyQuantity(ficha.getId())
                ))
                .toList();
    }

    @Transactional
    public ConsumirHoneyResponse consumirHoney(final ConsumirHoneyRequest request) {
        final Long idUsuario = currentUserId();
        final Ficha ficha = fichaQuery.findByIdForUpdate(request.idFicha());
        validateOwnership(ficha, idUsuario);

        final String operationId = request.idOperacao().toString();
        if (fichaQuery.findHistoricoByFichaAndLote(ficha.getId(), operationId).isPresent()) {
            return response(ficha, honeyQuantity(ficha.getId()), true);
        }

        final List<FichaItem> honeyItems = honeyItems(ficha.getId());
        final int totalHoneyBeforeConsumption = honeyItems.stream().mapToInt(this::quantity).sum();
        final FichaItem honey = honeyItems.stream()
                .filter(item -> quantity(item) > 0)
                .min(Comparator.comparing(FichaItem::getOrdem, Comparator.nullsLast(Comparator.naturalOrder())))
                .orElseThrow(() -> new BusinessException(
                        "Esta ficha nao possui Honey disponivel.",
                        "HONEY_NOT_AVAILABLE"
                ));

        final int previousQuantity = quantity(honey);
        final int remainingQuantity = previousQuantity - 1;
        if (remainingQuantity == 0) {
            fichaCommand.deleteItem(honey);
        } else {
            fichaCommand.saveItens(List.of(FichaItem.Builder.from(honey)
                    .quantidade(remainingQuantity)
                    .build()));
        }

        historicoWriter.recordHoneyUse(
                ficha.getId(),
                ficha.getIdOrganizacao(),
                request.idOperacao(),
                previousQuantity,
                remainingQuantity
        );
        return response(ficha, totalHoneyBeforeConsumption - 1, false);
    }

    private int honeyQuantity(final Long idFicha) {
        return honeyItems(idFicha).stream().mapToInt(this::quantity).sum();
    }

    private List<FichaItem> honeyItems(final Long idFicha) {
        return fichaQuery.findDetalhes(idFicha).itens().stream()
                .filter(this::isHoney)
                .toList();
    }

    private boolean isHoney(final FichaItem item) {
        return "honey".equals(normalize(item.getCodigo())) || "honey".equals(normalize(item.getNome()));
    }

    private String normalize(final String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^a-zA-Z0-9]", "")
                .toLowerCase(Locale.ROOT);
    }

    private int quantity(final FichaItem item) {
        return item.getQuantidade() == null ? 0 : Math.max(0, item.getQuantidade());
    }

    private void validateOwnership(final Ficha ficha, final Long idUsuario) {
        if (ficha.isNpc() || !idUsuario.equals(ficha.getIdUsuario())) {
            throw new BusinessException("A ficha escolhida nao pertence a sua conta.", "FICHA_NOT_OWNED");
        }
    }

    private Long currentUserId() {
        return userAccess.getId()
                .orElseThrow(() -> new BusinessException("Usuario atual nao identificado."));
    }

    private ConsumirHoneyResponse response(final Ficha ficha,
                                            final int remainingQuantity,
                                            final boolean repeatedOperation) {
        return new ConsumirHoneyResponse(
                ficha.getId(), ficha.getNome(), 1, remainingQuantity, HONEY_BONUS, repeatedOperation
        );
    }
}
