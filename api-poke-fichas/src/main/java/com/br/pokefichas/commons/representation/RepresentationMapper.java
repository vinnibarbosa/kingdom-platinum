package com.br.pokefichas.commons.representation;

import com.br.pokefichas.commons.page.Sort;
import com.br.pokefichas.commons.representation.cache.RepresentationCache;
import com.br.pokefichas.commons.representation.model.Representation;
import com.br.pokefichas.commons.representation.model.RepresentationProvider;
import com.br.pokefichas.commons.representation.parser.FilterParser;
import com.br.pokefichas.commons.representation.parser.SortParser;
import com.querydsl.core.BooleanBuilder;
import org.springframework.stereotype.Component;

@Component
public class RepresentationMapper {

    private final RepresentationCache cache;
    private final FilterParser filterParser;
    private final SortParser sortParser;

    public RepresentationMapper(final RepresentationCache cache,
                                final FilterParser filterParser,
                                final SortParser sortParser) {
        this.cache = cache;
        this.filterParser = filterParser;
        this.sortParser = sortParser;
    }

    public BooleanBuilder parseFilter(final Class<? extends RepresentationProvider<?, ?>> providerClass,
                                      final String rawFilter) {
        final Representation<?, ?> representation = cache.get(providerClass);
        return filterParser.parse(rawFilter, representation);
    }

    public Sort parseSort(final Class<? extends RepresentationProvider<?, ?>> providerClass,
                          final String rawSort) {
        final Representation<?, ?> representation = cache.get(providerClass);
        return sortParser.parse(rawSort, representation);
    }
}
