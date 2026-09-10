package com.br.pokefichas.domain.core.integracao.usecase;

import com.br.pokefichas.commons.exception.BusinessException;
import com.br.pokefichas.commons.useraccess.UserAccess;
import com.br.pokefichas.domain.core.ficha.model.Ficha;
import com.br.pokefichas.domain.core.ficha.model.FichaDetalhes;
import com.br.pokefichas.domain.core.ficha.model.FichaHistorico;
import com.br.pokefichas.domain.core.ficha.model.FichaItem;
import com.br.pokefichas.domain.core.ficha.repository.FichaCommand;
import com.br.pokefichas.domain.core.ficha.repository.FichaQuery;
import com.br.pokefichas.domain.core.ficha.usecase.FichaHistoricoWriter;
import com.br.pokefichas.domain.core.integracao.dto.ConsumirHoneyRequest;
import com.br.pokefichas.domain.core.integracao.dto.ConsumirBonusRolagemRequest;
import com.br.pokefichas.domain.core.integracao.dto.ConsumirShinyCharmRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class IntegrarRolagemUseCaseTest {

    @Mock private FichaQuery fichaQuery;
    @Mock private FichaCommand fichaCommand;
    @Mock private FichaHistoricoWriter historicoWriter;
    @Mock private UserAccess userAccess;

    private IntegrarRolagemUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new IntegrarRolagemUseCase(fichaQuery, fichaCommand, historicoWriter, userAccess);
        when(userAccess.getId()).thenReturn(Optional.of(7L));
    }

    @Test
    void shouldConsumeOneHoneyAndReturnRemainingQuantity() {
        final UUID operationId = UUID.randomUUID();
        final Ficha ficha = ficha(10L, 7L);
        final FichaItem honey = item(20L, "Honey", "honey", 2);
        givenConsumption(ficha, operationId, List.of(honey));

        final var response = useCase.consumirHoney(new ConsumirHoneyRequest(10L, operationId));

        assertThat(response.quantidadeConsumida()).isEqualTo(1);
        assertThat(response.quantidadeRestante()).isEqualTo(1);
        assertThat(response.bonusEncontros()).isEqualTo(2);
        assertThat(response.operacaoRepetida()).isFalse();
        verify(fichaCommand).saveItens(anyList());
        verify(fichaCommand, never()).deleteItem(honey);
        verify(historicoWriter).recordHoneyUse(10L, 3L, operationId, 2, 1);
    }

    @Test
    void shouldDeleteLastHoney() {
        final UUID operationId = UUID.randomUUID();
        final Ficha ficha = ficha(10L, 7L);
        final FichaItem honey = item(20L, "Honey", null, 1);
        givenConsumption(ficha, operationId, List.of(honey));

        final var response = useCase.consumirHoney(new ConsumirHoneyRequest(10L, operationId));

        assertThat(response.quantidadeRestante()).isZero();
        verify(fichaCommand).deleteItem(honey);
        verify(fichaCommand, never()).saveItens(anyList());
    }

    @Test
    void shouldNotConsumeTwiceForSameOperation() {
        final UUID operationId = UUID.randomUUID();
        final Ficha ficha = ficha(10L, 7L);
        final FichaItem honey = item(20L, "Honey", "honey", 3);
        when(fichaQuery.findByIdForUpdate(10L)).thenReturn(ficha);
        when(fichaQuery.findHistoricoByFichaAndLote(10L, operationId.toString()))
                .thenReturn(Optional.of(mock(FichaHistorico.class)));
        final FichaDetalhes detalhes = details(List.of(honey));
        when(fichaQuery.findDetalhes(10L)).thenReturn(detalhes);

        final var response = useCase.consumirHoney(new ConsumirHoneyRequest(10L, operationId));

        assertThat(response.operacaoRepetida()).isTrue();
        assertThat(response.quantidadeRestante()).isEqualTo(3);
        verify(fichaCommand, never()).saveItens(anyList());
        verify(fichaCommand, never()).deleteItem(honey);
    }

    @Test
    void shouldRejectWhenFichaHasNoHoney() {
        final UUID operationId = UUID.randomUUID();
        final Ficha ficha = ficha(10L, 7L);
        givenConsumption(ficha, operationId, List.of());

        assertThatThrownBy(() -> useCase.consumirHoney(new ConsumirHoneyRequest(10L, operationId)))
                .isInstanceOf(BusinessException.class)
                .extracting(error -> ((BusinessException) error).getCode())
                .isEqualTo("HONEY_NOT_AVAILABLE");
    }

    @Test
    void shouldRejectFichaOwnedByAnotherAccount() {
        final UUID operationId = UUID.randomUUID();
        final Ficha ficha = ficha(10L, 99L);
        when(fichaQuery.findByIdForUpdate(10L)).thenReturn(ficha);

        assertThatThrownBy(() -> useCase.consumirHoney(new ConsumirHoneyRequest(10L, operationId)))
                .isInstanceOf(BusinessException.class)
                .extracting(error -> ((BusinessException) error).getCode())
                .isEqualTo("FICHA_NOT_OWNED");
    }

    @Test
    void shouldConsumeShinyCharmForOwnedFicha() {
        final UUID operationId = UUID.randomUUID();
        final Ficha ficha = ficha(10L, 7L);
        final FichaItem shinyCharm = item(21L, "Shiny Charm", "shiny-charm", 2);
        givenConsumption(ficha, operationId, List.of(shinyCharm));

        final var response = useCase.consumirShinyCharm(new ConsumirShinyCharmRequest(10L, operationId));

        assertThat(response.quantidadeRestante()).isEqualTo(1);
        verify(fichaCommand).saveItens(anyList());
        verify(historicoWriter).recordShinyCharmUse(10L, 3L, operationId, 2, 1);
    }

    @Test
    void shouldRejectWhenFichaHasNoShinyCharm() {
        final UUID operationId = UUID.randomUUID();
        final Ficha ficha = ficha(10L, 7L);
        givenConsumption(ficha, operationId, List.of());

        assertThatThrownBy(() -> useCase.consumirShinyCharm(new ConsumirShinyCharmRequest(10L, operationId)))
                .isInstanceOf(BusinessException.class)
                .extracting(error -> ((BusinessException) error).getCode())
                .isEqualTo("SHINY_CHARM_NOT_AVAILABLE");
    }

    @Test
    void shouldConsumeHoneyAndShinyCharmAtomically() {
        final UUID operationId = UUID.randomUUID();
        final Ficha ficha = ficha(10L, 7L);
        final FichaItem honey = item(20L, "Honey", "honey", 2);
        final FichaItem shinyCharm = item(21L, "Shiny Charm", "shiny-charm", 1);
        givenConsumption(ficha, operationId, List.of(honey, shinyCharm));

        final var response = useCase.consumirBonus(
                new ConsumirBonusRolagemRequest(10L, operationId, true, true)
        );

        assertThat(response.quantidadeHoneyRestante()).isEqualTo(1);
        assertThat(response.quantidadeShinyCharmRestante()).isZero();
        verify(fichaCommand).deleteItem(shinyCharm);
        verify(fichaCommand).saveItens(anyList());
        verify(historicoWriter).recordRollBonusUse(10L, 3L, operationId, 2, 1);
    }

    @Test
    void shouldNotConsumeHoneyWhenShinyCharmIsMissing() {
        final UUID operationId = UUID.randomUUID();
        final Ficha ficha = ficha(10L, 7L);
        final FichaItem honey = item(20L, "Honey", "honey", 2);
        givenConsumption(ficha, operationId, List.of(honey));

        assertThatThrownBy(() -> useCase.consumirBonus(
                new ConsumirBonusRolagemRequest(10L, operationId, true, true)
        ))
                .isInstanceOf(BusinessException.class)
                .extracting(error -> ((BusinessException) error).getCode())
                .isEqualTo("SHINY_CHARM_NOT_AVAILABLE");
        verify(fichaCommand, never()).saveItens(anyList());
        verify(fichaCommand, never()).deleteItem(honey);
    }

    private void givenConsumption(final Ficha ficha, final UUID operationId, final List<FichaItem> items) {
        final FichaDetalhes detalhes = details(items);
        when(fichaQuery.findByIdForUpdate(ficha.getId())).thenReturn(ficha);
        when(fichaQuery.findHistoricoByFichaAndLote(ficha.getId(), operationId.toString())).thenReturn(Optional.empty());
        when(fichaQuery.findDetalhes(ficha.getId())).thenReturn(detalhes);
    }

    private Ficha ficha(final Long id, final Long ownerId) {
        final Ficha ficha = mock(Ficha.class);
        lenient().when(ficha.getId()).thenReturn(id);
        lenient().when(ficha.getIdUsuario()).thenReturn(ownerId);
        lenient().when(ficha.getIdOrganizacao()).thenReturn(3L);
        return ficha;
    }

    private FichaItem item(final Long id, final String name, final String code, final int quantity) {
        final FichaItem item = mock(FichaItem.class);
        lenient().when(item.getId()).thenReturn(id);
        lenient().when(item.getNome()).thenReturn(name);
        lenient().when(item.getCodigo()).thenReturn(code);
        lenient().when(item.getQuantidade()).thenReturn(quantity);
        lenient().when(item.getOrdem()).thenReturn(0);
        return item;
    }

    private FichaDetalhes details(final List<FichaItem> items) {
        final FichaDetalhes details = mock(FichaDetalhes.class);
        when(details.itens()).thenReturn(items);
        return details;
    }
}
