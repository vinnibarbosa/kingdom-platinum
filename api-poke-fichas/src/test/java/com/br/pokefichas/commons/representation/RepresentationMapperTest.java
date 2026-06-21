package com.br.pokefichas.commons.representation;

import com.br.pokefichas.commons.page.Sort;
import com.br.pokefichas.commons.representation.cache.RepresentationCache;
import com.br.pokefichas.commons.representation.exception.RepresentationException;
import com.br.pokefichas.commons.representation.model.Representation;
import com.br.pokefichas.commons.representation.model.RepresentationProvider;
import com.br.pokefichas.commons.representation.parser.FilterParser;
import com.br.pokefichas.commons.representation.parser.SortParser;
import com.br.pokefichas.commons.representation.resolver.ExpressionOperationResolver;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RepresentationMapperTest {

    private RepresentationMapper mapper;

    @BeforeEach
    void setUp() {
        final RepresentationCache cache = new RepresentationCache();
        final FilterParser filterParser = new FilterParser(new ExpressionOperationResolver());
        final SortParser sortParser = new SortParser();
        mapper = new RepresentationMapper(cache, filterParser, sortParser);
    }

    @Test
    void shouldParseFilterUsingProvider() {
        final BooleanBuilder result = mapper.parseFilter(SampleProvider.class, "nome = \"Empresa\"");
        assertThat(result.hasValue()).isTrue();
    }

    @Test
    void shouldReturnEmptyFilterForNull() {
        final BooleanBuilder result = mapper.parseFilter(SampleProvider.class, null);
        assertThat(result.hasValue()).isFalse();
    }

    @Test
    void shouldParseSortUsingProvider() {
        final Sort result = mapper.parseSort(SampleProvider.class, "nome asc");
        assertThat(result).isNotNull();
        assertThat(result.isEmpty()).isFalse();
    }

    @Test
    void shouldReturnDefaultSortForNullSort() {
        final Sort result = mapper.parseSort(SampleProvider.class, null);
        assertThat(result).isEqualTo(SampleProvider.DEFAULT_SORT);
    }

    @Test
    void shouldCacheRepresentationBetweenCalls() {
        mapper.parseFilter(SampleProvider.class, null);
        mapper.parseFilter(SampleProvider.class, null);
    }

    @Test
    void shouldRejectUnknownFilterField() {
        assertThatThrownBy(() -> mapper.parseFilter(SampleProvider.class, "inexistente = 1"))
                .isInstanceOf(RepresentationException.class);
    }

    public static class SampleProvider implements RepresentationProvider<Object, Object> {

        static final StringPath nomePath = Expressions.stringPath("nome");
        static final NumberPath<Long> idPath = Expressions.numberPath(Long.class, "id");
        static final Sort DEFAULT_SORT = Sort.of(idPath.asc());

        @Override
        public Representation<Object, Object> getRepresentation() {
            return RepresentationBuilder.create(Object.class, Object.class)
                    .filterableAndSortable("nome", nomePath)
                    .filterableAndSortable("id", idPath)
                    .defaultSort(DEFAULT_SORT)
                    .build();
        }
    }
}
