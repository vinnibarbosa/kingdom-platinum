package com.br.pokefichas.domain.core.ficha.usecase;

import com.br.pokefichas.commons.exception.EntityNotFoundException;
import com.br.pokefichas.domain.core.ficha.dto.FichaTimelineEntryRequest;
import com.br.pokefichas.domain.core.ficha.dto.FichaTimelineEntryResponse;
import com.br.pokefichas.domain.core.ficha.model.Ficha;
import com.br.pokefichas.domain.core.ficha.model.FichaTimelineEntry;
import com.br.pokefichas.domain.core.ficha.repository.FichaQuery;
import com.br.pokefichas.domain.core.ficha.repository.FichaTimelineCommand;
import com.br.pokefichas.domain.core.ficha.repository.FichaTimelineQuery;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
public class GerenciarFichaTimelineUseCase {

    private final FichaQuery fichaQuery;
    private final FichaTimelineQuery timelineQuery;
    private final FichaTimelineCommand timelineCommand;

    public GerenciarFichaTimelineUseCase(final FichaQuery fichaQuery,
                                         final FichaTimelineQuery timelineQuery,
                                         final FichaTimelineCommand timelineCommand) {
        this.fichaQuery = fichaQuery;
        this.timelineQuery = timelineQuery;
        this.timelineCommand = timelineCommand;
    }

    @Transactional(readOnly = true)
    public List<FichaTimelineEntryResponse> buscar(final Long idFicha) {
        buscarFicha(idFicha);
        return timelineQuery.findByFichaWithoutContext(idFicha).stream().map(this::toResponse).toList();
    }

    @Transactional
    public List<FichaTimelineEntryResponse> substituir(final Long idFicha,
                                                        final List<FichaTimelineEntryRequest> requests) {
        final Ficha ficha = buscarFicha(idFicha);
        timelineCommand.deleteByFichaWithoutContext(idFicha);

        final List<FichaTimelineEntry> entries = Optional.ofNullable(requests).orElse(List.of()).stream()
                .map(request -> FichaTimelineEntry.Builder.create()
                        .idOrganizacao(ficha.getIdOrganizacao())
                        .idFicha(idFicha)
                        .secao(request.secao().trim())
                        .periodo(trimToNull(request.periodo()))
                        .titulo(request.titulo().trim())
                        .subtitulo(trimToNull(request.subtitulo()))
                        .conteudo(request.conteudo().trim())
                        .cor(trimToNull(request.cor()))
                        .ordem(Optional.ofNullable(request.ordem()).orElse(0))
                        .build())
                .toList();

        if (entries.isEmpty()) {
            return List.of();
        }

        return timelineCommand.saveWithoutContext(entries).stream().map(this::toResponse).toList();
    }

    private Ficha buscarFicha(final Long idFicha) {
        return fichaQuery.findByIdWithoutContext(idFicha)
                .orElseThrow(() -> new EntityNotFoundException("Ficha nao encontrada: " + idFicha));
    }

    private String trimToNull(final String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private FichaTimelineEntryResponse toResponse(final FichaTimelineEntry entry) {
        return new FichaTimelineEntryResponse(
                entry.getId(), entry.getSecao(), entry.getPeriodo(), entry.getTitulo(),
                entry.getSubtitulo(), entry.getConteudo(), entry.getCor(), entry.getOrdem()
        );
    }
}
