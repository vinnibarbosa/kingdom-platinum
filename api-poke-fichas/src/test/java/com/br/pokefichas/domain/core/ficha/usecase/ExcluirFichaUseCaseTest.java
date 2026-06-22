package com.br.pokefichas.domain.core.ficha.usecase;

import com.br.pokefichas.commons.exception.UnauthorizedException;
import com.br.pokefichas.commons.useraccess.UserAccess;
import com.br.pokefichas.domain.core.ficha.model.Ficha;
import com.br.pokefichas.domain.core.ficha.repository.FichaCommand;
import com.br.pokefichas.domain.core.ficha.repository.FichaQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExcluirFichaUseCaseTest {

    @Mock
    private FichaCommand command;

    @Mock
    private FichaQuery query;

    @Mock
    private UserAccess userAccess;

    @InjectMocks
    private ExcluirFichaUseCase useCase;

    @Test
    void shouldRejectNonAdminUser() {
        when(userAccess.getRole()).thenReturn(Optional.of("OPERADOR"));

        assertThatThrownBy(() -> useCase.handle(10L))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Apenas administradores podem excluir fichas");

        verifyNoInteractions(command, query);
    }

    @Test
    void shouldAllowAdminUser() {
        final Ficha ficha = mock(Ficha.class);
        when(userAccess.getRole()).thenReturn(Optional.of("ADMIN"));
        when(query.findByIdWithoutContext(10L)).thenReturn(Optional.of(ficha));

        useCase.handle(10L);

        verify(command).delete(ficha);
    }
}
