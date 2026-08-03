package com.br.pokefichas.domain.core.ficha.usecase;

import com.br.pokefichas.domain.core.ficha.dto.FichaHistoricoGlobalResponse;
import com.br.pokefichas.domain.core.ficha.model.Ficha;
import com.br.pokefichas.domain.core.ficha.model.FichaHistorico;
import com.br.pokefichas.domain.core.ficha.repository.FichaQuery;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ListarHistoricoGlobalUseCase {

    private final FichaQuery query;

    public ListarHistoricoGlobalUseCase(final FichaQuery query) {
        this.query = query;
    }

    @Transactional(readOnly = true)
    public java.util.List<FichaHistoricoGlobalResponse> handle() {
        final Map<Long, String> nomesPorFicha = query.findAllWithoutContext().stream()
                .collect(Collectors.toMap(Ficha::getId, Ficha::getNome, (first, ignored) -> first));

        return query.findAllHistoricosWithoutContext().stream()
                .map(historico -> toResponse(historico, nomesPorFicha.get(historico.getIdFicha())))
                .toList();
    }

    private FichaHistoricoGlobalResponse toResponse(final FichaHistorico historico, final String nomeFicha) {
        return new FichaHistoricoGlobalResponse(
                historico.getId(),
                historico.getIdFicha(),
                nomeFicha == null || nomeFicha.isBlank() ? "Ficha removida" : nomeFicha,
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
