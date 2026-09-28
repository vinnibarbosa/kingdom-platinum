package com.br.pokefichas.domain.core.ficha.usecase;

import com.br.pokefichas.commons.exception.BusinessException;
import com.br.pokefichas.domain.core.ficha.dto.FichaResponse;
import com.br.pokefichas.domain.core.ficha.model.Ficha;
import com.br.pokefichas.domain.core.ficha.model.FichaMapper;
import com.br.pokefichas.domain.core.ficha.repository.FichaCommand;
import com.br.pokefichas.domain.core.ficha.repository.FichaQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GerenciarFalecimentoFichaUseCaseTest {

    private static final Long FICHA_ID = 10L;
    private static final Long DONO_ID = 20L;

    @Mock private FichaQuery query;
    @Mock private FichaCommand command;
    @Mock private FichaMapper mapper;
    @Mock private FichaHistoricoWriter historicoWriter;
    @InjectMocks private GerenciarFalecimentoFichaUseCase useCase;

    @Test
    void moverParaMemorialLiberaVagaSemApagarFicha() {
        final Ficha ficha = ficha(false);
        final FichaResponse before = mock(FichaResponse.class);
        final FichaResponse after = mock(FichaResponse.class);
        when(query.findByIdWithoutContext(FICHA_ID)).thenReturn(Optional.of(ficha));
        when(mapper.toResponse(eq(ficha), isNull())).thenReturn(before, after);
        when(command.saveWithoutContext(ficha)).thenReturn(ficha);

        assertThat(useCase.alterar(FICHA_ID, true)).isSameAs(after);

        assertThat(ficha.isFalecida()).isTrue();
        verify(command).saveWithoutContext(ficha);
        verify(historicoWriter).recordUpdateWithoutContext(before, after);
        verify(query, never()).countAtivasByUsuarioWithoutContext(any());
    }

    @Test
    void restaurarQuandoHaVaga() {
        final Ficha ficha = ficha(true);
        when(query.findByIdWithoutContext(FICHA_ID)).thenReturn(Optional.of(ficha));
        when(query.countAtivasByUsuarioWithoutContext(DONO_ID)).thenReturn(1L);
        when(command.saveWithoutContext(ficha)).thenReturn(ficha);

        useCase.alterar(FICHA_ID, false);

        assertThat(ficha.isFalecida()).isFalse();
        verify(command).saveWithoutContext(ficha);
    }

    @Test
    void naoRestaurarAcimaDoLimiteDeDuasAtivas() {
        final Ficha ficha = ficha(true);
        when(query.findByIdWithoutContext(FICHA_ID)).thenReturn(Optional.of(ficha));
        when(query.countAtivasByUsuarioWithoutContext(DONO_ID)).thenReturn(2L);

        assertThatThrownBy(() -> useCase.alterar(FICHA_ID, false))
                .isInstanceOf(BusinessException.class)
                .hasMessage("O dono ja possui 2 fichas ativas.");

        assertThat(ficha.isFalecida()).isTrue();
        verify(command, never()).saveWithoutContext(any());
    }

    private Ficha ficha(final boolean falecida) {
        return Ficha.Builder.create()
                .nome("Personagem")
                .idUsuario(DONO_ID)
                .falecida(falecida)
                .build(false);
    }
}
