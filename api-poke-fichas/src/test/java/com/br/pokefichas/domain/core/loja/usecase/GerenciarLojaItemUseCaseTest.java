package com.br.pokefichas.domain.core.loja.usecase;

import com.br.pokefichas.commons.organizacao.OrganizacaoContext;
import com.br.pokefichas.domain.core.loja.dto.ImportarCatalogoLojaRequest;
import com.br.pokefichas.domain.core.loja.dto.LojaItemRequest;
import com.br.pokefichas.domain.core.loja.model.LojaItem;
import com.br.pokefichas.domain.core.loja.model.LojaMapper;
import com.br.pokefichas.domain.core.loja.repository.LojaItemCommand;
import com.br.pokefichas.domain.core.loja.repository.LojaItemQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GerenciarLojaItemUseCaseTest {

    @Mock private LojaItemCommand command;
    @Mock private LojaItemQuery query;
    @Mock private LojaMapper mapper;
    @Mock private OrganizacaoContext organizacaoContext;

    private GerenciarLojaItemUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GerenciarLojaItemUseCase(command, query, mapper, organizacaoContext);
        when(organizacaoContext.getRequiredOrganizacaoId()).thenReturn(1L);
        when(command.save(any(LojaItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void shouldRemoveOnlyCatalogItemsMissingFromSource() {
        final LojaItem current = item(1L, "honey", "Honey", true);
        final LojaItem stale = item(2L, "old-item", "Old Item", true);
        final LojaItem manual = item(3L, "custom-item", "Custom Item", false);
        when(query.findTodos()).thenReturn(List.of(current, stale, manual));

        final LojaItemRequest sourceItem = new LojaItemRequest(
                "Berries", "honey", null, "Honey", "Descricao atualizada",
                BigDecimal.valueOf(500), true, 0
        );

        final var response = useCase.importarCatalogo(new ImportarCatalogoLojaRequest(List.of(sourceItem)));

        assertThat(response.importados()).isZero();
        assertThat(response.atualizados()).isEqualTo(1);
        assertThat(response.removidos()).isEqualTo(1);
        verify(command).delete(stale);
        verify(command, never()).delete(manual);
        verify(command, never()).delete(current);
    }

    private LojaItem item(final Long id, final String code, final String name, final boolean catalogManaged) {
        final LojaItem item = org.mockito.Mockito.mock(LojaItem.class);
        lenient().when(item.getId()).thenReturn(id);
        lenient().when(item.getCodigo()).thenReturn(code);
        lenient().when(item.getNome()).thenReturn(name);
        lenient().when(item.isGerenciadoCatalogo()).thenReturn(catalogManaged);
        return item;
    }
}
