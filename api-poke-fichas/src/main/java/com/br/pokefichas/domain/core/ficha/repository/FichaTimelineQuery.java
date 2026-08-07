package com.br.pokefichas.domain.core.ficha.repository;

import com.br.pokefichas.commons.persistence.JpaRepository;
import com.br.pokefichas.domain.core.ficha.model.FichaTimelineEntry;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class FichaTimelineQuery {

    private final JpaRepository repository;

    public FichaTimelineQuery(final JpaRepository repository) {
        this.repository = repository;
    }

    public List<FichaTimelineEntry> findByFichaWithoutContext(final Long idFicha) {
        return repository.findAllWithoutTenantFilter(FichaTimelineEntry.class).stream()
                .filter(entry -> idFicha.equals(entry.getIdFicha()))
                .sorted(Comparator.comparing(FichaTimelineEntry::getOrdem, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(FichaTimelineEntry::getId, Comparator.nullsLast(Long::compareTo)))
                .toList();
    }
}
