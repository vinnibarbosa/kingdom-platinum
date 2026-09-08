package com.br.pokefichas.domain.core.ficha.repository;

import com.br.pokefichas.commons.persistence.JpaRepository;
import com.br.pokefichas.commons.page.Sort;
import com.br.pokefichas.domain.core.ficha.model.FichaTimelineEntry;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.br.pokefichas.domain.core.ficha.model.QFichaTimelineEntry.fichaTimelineEntry;

@Component
public class FichaTimelineQuery {

    private final JpaRepository repository;

    public FichaTimelineQuery(final JpaRepository repository) {
        this.repository = repository;
    }

    public List<FichaTimelineEntry> findByFichaWithoutContext(final Long idFicha) {
        return repository.findAllWithoutTenantFilter(
                FichaTimelineEntry.class,
                Sort.of(fichaTimelineEntry.ordem.asc(), fichaTimelineEntry.id.asc()),
                fichaTimelineEntry.idFicha.eq(idFicha)
        );
    }
}
