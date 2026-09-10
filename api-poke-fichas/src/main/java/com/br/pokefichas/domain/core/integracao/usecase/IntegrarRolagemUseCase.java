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
import com.br.pokefichas.domain.core.integracao.dto.ConsumirBonusRolagemRequest;
import com.br.pokefichas.domain.core.integracao.dto.ConsumirBonusRolagemResponse;
import com.br.pokefichas.domain.core.integracao.dto.ConsumirShinyCharmRequest;
import com.br.pokefichas.domain.core.integracao.dto.ConsumirShinyCharmResponse;
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
                .map(this::toRolagemResponse)
                .toList();
    }

    @Transactional
    public ConsumirShinyCharmResponse consumirShinyCharm(final ConsumirShinyCharmRequest request) {
        final Long idUsuario = currentUserId();
        final Ficha ficha = fichaQuery.findByIdForUpdate(request.idFicha());
        validateOwnership(ficha, idUsuario);

        final String operationId = request.idOperacao().toString();
        if (fichaQuery.findHistoricoByFichaAndLote(ficha.getId(), operationId).isPresent()) {
            return shinyCharmResponse(ficha, shinyCharmQuantity(ficha.getId()), true);
        }

        final List<FichaItem> shinyCharmItems = shinyCharmItems(ficha.getId());
        final int totalBeforeConsumption = shinyCharmItems.stream().mapToInt(this::quantity).sum();
        final FichaItem shinyCharm = shinyCharmItems.stream()
                .filter(item -> quantity(item) > 0)
                .min(Comparator.comparing(FichaItem::getOrdem, Comparator.nullsLast(Comparator.naturalOrder())))
                .orElseThrow(() -> new BusinessException(
                        "Esta ficha nao possui Shiny Charm disponivel.",
                        "SHINY_CHARM_NOT_AVAILABLE"
                ));

        final int previousQuantity = quantity(shinyCharm);
        final int remainingQuantity = previousQuantity - 1;
        if (remainingQuantity == 0) {
            fichaCommand.deleteItem(shinyCharm);
        } else {
            fichaCommand.saveItens(List.of(FichaItem.Builder.from(shinyCharm)
                    .quantidade(remainingQuantity)
                    .build()));
        }

        historicoWriter.recordShinyCharmUse(
                ficha.getId(),
                ficha.getIdOrganizacao(),
                request.idOperacao(),
                previousQuantity,
                remainingQuantity
        );
        return shinyCharmResponse(ficha, totalBeforeConsumption - 1, false);
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

    @Transactional
    public ConsumirBonusRolagemResponse consumirBonus(final ConsumirBonusRolagemRequest request) {
        if (!request.usarHoney() && !request.usarShinyCharm()) {
            throw new BusinessException("Nenhum bonus foi selecionado.", "ROLL_BONUS_NOT_SELECTED");
        }

        final Long idUsuario = currentUserId();
        final Ficha ficha = fichaQuery.findByIdForUpdate(request.idFicha());
        validateOwnership(ficha, idUsuario);

        final String operationId = request.idOperacao().toString();
        if (fichaQuery.findHistoricoByFichaAndLote(ficha.getId(), operationId).isPresent()) {
            return bonusResponse(ficha, true);
        }

        final List<FichaItem> items = fichaQuery.findDetalhes(ficha.getId()).itens();
        final FichaItem honey = request.usarHoney()
                ? findAvailableItem(items, this::isHoney, "Esta ficha nao possui Honey disponivel.", "HONEY_NOT_AVAILABLE")
                : null;
        final FichaItem shinyCharm = request.usarShinyCharm()
                ? findAvailableItem(items, this::isShinyCharm, "Esta ficha nao possui Shiny Charm disponivel.", "SHINY_CHARM_NOT_AVAILABLE")
                : null;

        if (honey != null) {
            consumeItem(honey);
        }
        if (shinyCharm != null) {
            consumeItem(shinyCharm);
        }

        historicoWriter.recordRollBonusUse(
                ficha.getId(),
                ficha.getIdOrganizacao(),
                request.idOperacao(),
                honey == null ? null : quantity(honey),
                shinyCharm == null ? null : quantity(shinyCharm)
        );

        final int honeyRemaining = items.stream().filter(this::isHoney).mapToInt(this::quantity).sum()
                - (request.usarHoney() ? 1 : 0);
        final int shinyCharmRemaining = items.stream().filter(this::isShinyCharm).mapToInt(this::quantity).sum()
                - (request.usarShinyCharm() ? 1 : 0);
        return new ConsumirBonusRolagemResponse(
                ficha.getId(), ficha.getNome(), honeyRemaining, shinyCharmRemaining, false
        );
    }

    private FichaItem findAvailableItem(final List<FichaItem> items,
                                        final java.util.function.Predicate<FichaItem> matcher,
                                        final String message,
                                        final String code) {
        return items.stream()
                .filter(matcher)
                .filter(item -> quantity(item) > 0)
                .min(Comparator.comparing(FichaItem::getOrdem, Comparator.nullsLast(Comparator.naturalOrder())))
                .orElseThrow(() -> new BusinessException(message, code));
    }

    private void consumeItem(final FichaItem item) {
        final int remainingQuantity = quantity(item) - 1;
        if (remainingQuantity == 0) {
            fichaCommand.deleteItem(item);
        } else {
            fichaCommand.saveItens(List.of(FichaItem.Builder.from(item)
                    .quantidade(remainingQuantity)
                    .build()));
        }
    }

    private int honeyQuantity(final Long idFicha) {
        return honeyItems(idFicha).stream().mapToInt(this::quantity).sum();
    }

    private FichaRolagemResponse toRolagemResponse(final Ficha ficha) {
        final List<FichaItem> items = fichaQuery.findDetalhes(ficha.getId()).itens();
        return new FichaRolagemResponse(
                ficha.getId(),
                ficha.getNome(),
                ficha.getRanking(),
                items.stream().filter(this::isHoney).mapToInt(this::quantity).sum(),
                items.stream().filter(this::isShinyCharm).mapToInt(this::quantity).sum()
        );
    }

    private List<FichaItem> honeyItems(final Long idFicha) {
        return fichaQuery.findDetalhes(idFicha).itens().stream()
                .filter(this::isHoney)
                .toList();
    }

    private boolean isHoney(final FichaItem item) {
        return "honey".equals(normalize(item.getCodigo())) || "honey".equals(normalize(item.getNome()));
    }

    private int shinyCharmQuantity(final Long idFicha) {
        return shinyCharmItems(idFicha).stream().mapToInt(this::quantity).sum();
    }

    private List<FichaItem> shinyCharmItems(final Long idFicha) {
        return fichaQuery.findDetalhes(idFicha).itens().stream()
                .filter(this::isShinyCharm)
                .toList();
    }

    private boolean isShinyCharm(final FichaItem item) {
        return "shinycharm".equals(normalize(item.getCodigo()))
                || "shinycharm".equals(normalize(item.getNome()));
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

    private ConsumirShinyCharmResponse shinyCharmResponse(final Ficha ficha,
                                                           final int remainingQuantity,
                                                           final boolean repeatedOperation) {
        return new ConsumirShinyCharmResponse(
                ficha.getId(), ficha.getNome(), 1, remainingQuantity, repeatedOperation
        );
    }

    private ConsumirBonusRolagemResponse bonusResponse(final Ficha ficha, final boolean repeatedOperation) {
        return new ConsumirBonusRolagemResponse(
                ficha.getId(),
                ficha.getNome(),
                honeyQuantity(ficha.getId()),
                shinyCharmQuantity(ficha.getId()),
                repeatedOperation
        );
    }
}
