package com.br.pokefichas.commons.representation;

import com.br.pokefichas.commons.page.Sort;
import com.br.pokefichas.commons.representation.cache.RepresentationCache;
import com.br.pokefichas.commons.representation.exception.RepresentationException;
import com.br.pokefichas.commons.representation.model.Representation;
import com.br.pokefichas.commons.representation.model.RepresentationProvider;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class RepresentationInfrastructureIntegrationTest {

    @Autowired
    private RepresentationMapper mapper;

    @Autowired
    private RepresentationCache cache;

    @Test
    void contextLoadsRepresentationBeans() {
        assertThat(mapper).isNotNull();
        assertThat(cache).isNotNull();
    }

    @Test
    void shouldParseFilterEndToEnd() {
        final BooleanBuilder result = mapper.parseFilter(NotaProvider.class, "chaveAcesso = \"ABC123\"");
        assertThat(result.hasValue()).isTrue();
    }

    @Test
    void shouldParseCompositeFilterEndToEnd() {
        final BooleanBuilder result = mapper.parseFilter(
                NotaProvider.class, "id in (1, 2, 3) and chaveAcesso like \"35%\"");
        assertThat(result.hasValue()).isTrue();
    }

    @Test
    void shouldParseSortEndToEnd() {
        final Sort result = mapper.parseSort(NotaProvider.class, "chaveAcesso asc");
        assertThat(result).isNotNull();
        assertThat(result.isEmpty()).isFalse();
    }

    @Test
    void shouldFallBackToDefaultSortWhenNoneProvided() {
        final Sort result = mapper.parseSort(NotaProvider.class, null);
        assertThat(result).isEqualTo(NotaProvider.DEFAULT_SORT);
    }

    @Test
    void shouldCacheRepresentationAcrossMultipleCalls() {
        final Representation<?, ?> first = cache.get(NotaProvider.class);
        final Representation<?, ?> second = cache.get(NotaProvider.class);
        assertThat(first).isSameAs(second);
    }

    @Test
    void shouldRejectFilterOnNonFilterableField() {
        assertThatThrownBy(() -> mapper.parseFilter(NotaProvider.class, "naoFiltravel = \"X\""))
                .isInstanceOf(RepresentationException.class)
                .hasMessageContaining("naoFiltravel");
    }

    @Test
    void shouldRejectSortOnNonSortableField() {
        assertThatThrownBy(() -> mapper.parseSort(NotaProvider.class, "naoOrdenavel asc"))
                .isInstanceOf(RepresentationException.class)
                .hasMessageContaining("naoOrdenavel");
    }

    public static class NotaProvider implements RepresentationProvider<Object, Object> {

        static final StringPath chaveAcesso = Expressions.stringPath("chaveAcesso");
        static final NumberPath<Long> id = Expressions.numberPath(Long.class, "id");
        static final Sort DEFAULT_SORT = Sort.of(id.desc());

        @Override
        public Representation<Object, Object> getRepresentation() {
            return RepresentationBuilder.create(Object.class, Object.class)
                    .filterableAndSortable("id", id)
                    .filterableAndSortable("chaveAcesso", chaveAcesso)
                    .defaultSort(DEFAULT_SORT)
                    .build();
        }
    }
}
