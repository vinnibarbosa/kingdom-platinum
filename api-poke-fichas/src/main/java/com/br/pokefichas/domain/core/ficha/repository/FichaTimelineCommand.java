package com.br.pokefichas.domain.core.ficha.repository;

import com.br.pokefichas.commons.persistence.JpaRepository;
import com.br.pokefichas.domain.core.ficha.model.FichaTimelineEntry;
import com.querydsl.core.types.dsl.PathBuilder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FichaTimelineCommand {

    private final JpaRepository repository;

    public FichaTimelineCommand(final JpaRepository repository) {
        this.repository = repository;
    }

    public List<FichaTimelineEntry> saveWithoutContext(final List<FichaTimelineEntry> entries) {
        return repository.saveAllWithoutContext(entries);
    }

    public void deleteByFichaWithoutContext(final Long idFicha) {
        final PathBuilder<FichaTimelineEntry> entry = new PathBuilder<>(FichaTimelineEntry.class, "fichaTimelineEntry");
        repository.deleteWithoutContext(FichaTimelineEntry.class, entry.getNumber("idFicha", Long.class).eq(idFicha));
    }
}
