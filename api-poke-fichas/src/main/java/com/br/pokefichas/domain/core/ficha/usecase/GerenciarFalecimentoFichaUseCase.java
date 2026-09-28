package com.br.pokefichas.domain.core.ficha.usecase;

import com.br.pokefichas.commons.exception.BusinessException;
import com.br.pokefichas.commons.exception.EntityNotFoundException;
import com.br.pokefichas.domain.core.ficha.dto.FichaResponse;
import com.br.pokefichas.domain.core.ficha.model.Ficha;
import com.br.pokefichas.domain.core.ficha.model.FichaMapper;
import com.br.pokefichas.domain.core.ficha.repository.FichaCommand;
import com.br.pokefichas.domain.core.ficha.repository.FichaQuery;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class GerenciarFalecimentoFichaUseCase {

    private final FichaQuery query;
    private final FichaCommand command;
    private final FichaMapper mapper;
    private final FichaHistoricoWriter historicoWriter;

    public GerenciarFalecimentoFichaUseCase(final FichaQuery query,
                                            final FichaCommand command,
                                            final FichaMapper mapper,
                                            final FichaHistoricoWriter historicoWriter) {
        this.query = query;
        this.command = command;
        this.mapper = mapper;
        this.historicoWriter = historicoWriter;
    }

    @Transactional
    public FichaResponse alterar(final Long id, final boolean falecida) {
        final Ficha ficha = query.findByIdWithoutContext(id)
                .orElseThrow(() -> new EntityNotFoundException("Ficha nao encontrada: " + id));
        if (ficha.isNpc()) {
            throw new BusinessException("Fichas de NPC nao fazem parte do Memorial.", "NPC_MEMORIAL_NOT_ALLOWED");
        }
        if (ficha.isFalecida() == falecida) {
            throw new BusinessException("A ficha ja esta neste estado.", "FICHA_STATUS_UNCHANGED");
        }
        if (!falecida && ficha.getIdUsuario() != null) {
            final long ativas = query.countAtivasByUsuarioWithoutContext(ficha.getIdUsuario());
            if (ativas >= 2) {
                throw new BusinessException("O dono ja possui 2 fichas ativas.", "FICHA_LIMIT_REACHED");
            }
        }

        final FichaResponse before = mapper.toResponse(ficha, query.findDetalhesWithoutContext(id));
        final Ficha saved = command.saveWithoutContext(Ficha.Builder.from(ficha).falecida(falecida).build(false));
        final FichaResponse after = mapper.toResponse(saved, query.findDetalhesWithoutContext(id));
        historicoWriter.recordUpdateWithoutContext(before, after);
        return after;
    }
}
