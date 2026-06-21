package com.br.pokefichas.domain.core.ficha.usecase;

import com.br.pokefichas.commons.dto.PageRequest;
import com.br.pokefichas.commons.page.Page;
import com.br.pokefichas.domain.core.ficha.dto.FichaResumoResponse;
import com.br.pokefichas.domain.core.ficha.model.Ficha;
import com.br.pokefichas.domain.core.ficha.model.FichaMapper;
import com.br.pokefichas.domain.core.ficha.model.FichaPokemon;
import com.br.pokefichas.domain.core.ficha.repository.FichaQuery;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ListarFichasUseCase {

    private final FichaQuery query;
    private final FichaMapper mapper;

    public ListarFichasUseCase(final FichaQuery query, final FichaMapper mapper) {
        this.query = query;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public Page<FichaResumoResponse> handle(final PageRequest pageRequest) {
        final Page<Ficha> fichas = query.findAllWithoutContext(pageRequest);
        final List<Long> idsFicha = fichas.getContent().stream().map(Ficha::getId).toList();
        final Map<Long, List<FichaPokemon>> equipePorFicha = query.findPokemonsWithoutContextByFichaIds(idsFicha).stream()
                .filter(this::isTeamPokemon)
                .sorted(Comparator
                        .comparing(FichaPokemon::getOrdem, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(FichaPokemon::getId, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.groupingBy(FichaPokemon::getIdFicha));
        return fichas.map(ficha -> mapper.toResumo(
                ficha,
                equipePorFicha.getOrDefault(ficha.getId(), List.of()).stream().limit(6).toList()
        ));
    }

    private boolean isTeamPokemon(final FichaPokemon pokemon) {
        final String location = pokemon.getBox() == null ? "" : pokemon.getBox().trim().toLowerCase(Locale.ROOT);
        return !location.equals("box") && !location.equals("pc");
    }
}
