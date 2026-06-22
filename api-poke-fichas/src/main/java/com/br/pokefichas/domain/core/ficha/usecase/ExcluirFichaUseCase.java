package com.br.pokefichas.domain.core.ficha.usecase;

import com.br.pokefichas.commons.exception.EntityNotFoundException;
import com.br.pokefichas.commons.exception.UnauthorizedException;
import com.br.pokefichas.commons.useraccess.UserAccess;
import com.br.pokefichas.domain.core.ficha.model.Ficha;
import com.br.pokefichas.domain.core.ficha.repository.FichaCommand;
import com.br.pokefichas.domain.core.ficha.repository.FichaQuery;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ExcluirFichaUseCase {

    private final FichaCommand command;
    private final FichaQuery query;
    private final UserAccess userAccess;

    public ExcluirFichaUseCase(final FichaCommand command,
                               final FichaQuery query,
                               final UserAccess userAccess) {
        this.command = command;
        this.query = query;
        this.userAccess = userAccess;
    }

    @Transactional
    public void handle(final Long id) {
        final boolean admin = userAccess.getRole().map("ADMIN"::equals).orElse(false);
        if (!admin) {
            throw new UnauthorizedException("Apenas administradores podem excluir fichas");
        }

        final Ficha ficha = query.findByIdWithoutContext(id)
                .orElseThrow(() -> new EntityNotFoundException("Ficha nao encontrada: " + id));

        command.delete(ficha);
    }
}
