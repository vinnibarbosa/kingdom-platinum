package com.br.pokefichas.domain.core.ficha.usecase;

import com.br.pokefichas.commons.exception.EntityNotFoundException;
import com.br.pokefichas.domain.core.ficha.dto.AtualizarFichaRequest;
import com.br.pokefichas.domain.core.ficha.dto.FichaResponse;
import com.br.pokefichas.domain.core.ficha.model.Ficha;
import com.br.pokefichas.domain.core.ficha.model.FichaDetalhes;
import com.br.pokefichas.domain.core.ficha.model.FichaMapper;
import com.br.pokefichas.domain.core.ficha.repository.FichaCommand;
import com.br.pokefichas.domain.core.ficha.repository.FichaQuery;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AtualizarFichaUseCase {

    private final FichaCommand command;
    private final FichaQuery query;
    private final FichaMapper mapper;
    private final FichaDetalhesWriter detalhesWriter;
    private final FichaHistoricoWriter historicoWriter;

    public AtualizarFichaUseCase(final FichaCommand command,
                                 final FichaQuery query,
                                 final FichaMapper mapper,
                                 final FichaDetalhesWriter detalhesWriter,
                                 final FichaHistoricoWriter historicoWriter) {
        this.command = command;
        this.query = query;
        this.mapper = mapper;
        this.detalhesWriter = detalhesWriter;
        this.historicoWriter = historicoWriter;
    }

    @Transactional
    public FichaResponse handle(final Long id, final AtualizarFichaRequest request) {
        final Ficha ficha = query.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ficha nao encontrada: " + id));
        final FichaResponse before = mapper.toResponse(ficha, query.findDetalhes(id));
        final Ficha saved = command.save(mapper.toEntity(ficha, request));
        detalhesWriter.replace(request, saved.getId(), saved.getIdOrganizacao());
        final FichaDetalhes detalhes = query.findDetalhes(saved.getId());
        final FichaResponse after = mapper.toResponse(saved, detalhes);
        historicoWriter.recordUpdate(before, after);
        return after;
    }
}
