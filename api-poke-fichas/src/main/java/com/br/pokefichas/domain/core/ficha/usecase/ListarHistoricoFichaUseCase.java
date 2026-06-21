package com.br.pokefichas.domain.core.ficha.usecase;

import com.br.pokefichas.commons.exception.EntityNotFoundException;
import com.br.pokefichas.domain.core.ficha.dto.FichaHistoricoResponse;
import com.br.pokefichas.domain.core.ficha.model.FichaHistorico;
import com.br.pokefichas.domain.core.ficha.repository.FichaQuery;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class ListarHistoricoFichaUseCase {

    private final FichaQuery query;

    public ListarHistoricoFichaUseCase(final FichaQuery query) {
        this.query = query;
    }

    @Transactional(readOnly = true)
    public List<FichaHistoricoResponse> handle(final Long idFicha) {
        query.findByIdWithoutContext(idFicha)
                .orElseThrow(() -> new EntityNotFoundException("Ficha nao encontrada: " + idFicha));
        return query.findHistoricosWithoutContext(idFicha).stream()
                .map(this::toResponse)
                .toList();
    }

    private FichaHistoricoResponse toResponse(final FichaHistorico historico) {
        return new FichaHistoricoResponse(
                historico.getId(),
                historico.getLote(),
                historico.getAcao(),
                historico.getCampo(),
                historico.getValorAnterior(),
                historico.getValorNovo(),
                historico.getCreatedAt(),
                historico.getCreatedBy()
        );
    }
}
