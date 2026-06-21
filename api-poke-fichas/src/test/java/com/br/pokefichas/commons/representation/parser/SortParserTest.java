package com.br.pokefichas.commons.representation.parser;

import com.br.pokefichas.commons.page.Sort;
import com.br.pokefichas.commons.representation.RepresentationBuilder;
import com.br.pokefichas.commons.representation.exception.RepresentationException;
import com.br.pokefichas.commons.representation.model.Representation;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SortParserTest {

    private SortParser parser;
    private Representation<?, ?> representation;
    private NumberPath<Long> idPath;
    private StringPath nomePath;

    @BeforeEach
    void setUp() {
        parser = new SortParser();

        idPath = Expressions.numberPath(Long.class, "id");
        nomePath = Expressions.stringPath("nome");

        representation = RepresentationBuilder.create(Object.class, Object.class)
                .sortable("id", idPath)
                .sortable("nome", nomePath)
                .defaultSort(Sort.of(idPath.desc()))
                .build();
    }

    @Test
    void shouldReturnDefaultSortForNull() {
        final Sort result = parser.parse(null, representation);
        assertThat(result).isEqualTo(representation.getDefaultSort());
    }

    @Test
    void shouldReturnDefaultSortForBlank() {
        final Sort result = parser.parse("  ", representation);
        assertThat(result).isEqualTo(representation.getDefaultSort());
    }

    @Test
    void shouldParseSingleFieldAsc() {
        final Sort result = parser.parse("nome asc", representation);
        assertThat(result).isNotNull();
        assertThat(result.isEmpty()).isFalse();
        assertThat(result.toString()).containsIgnoringCase("nome");
    }

    @Test
    void shouldParseSingleFieldDesc() {
        final Sort result = parser.parse("id desc", representation);
        assertThat(result).isNotNull();
        assertThat(result.toString()).containsIgnoringCase("id");
    }

    @Test
    void shouldParseDefaultDirectionAsAsc() {
        final Sort result = parser.parse("nome", representation);
        assertThat(result).isNotNull();
        assertThat(result.toString()).containsIgnoringCase("asc");
    }

    @Test
    void shouldParseMultipleFieldsWithComma() {
        final Sort result = parser.parse("nome asc,id desc", representation);
        assertThat(result).isNotNull();
        assertThat(result.isEmpty()).isFalse();
    }

    @Test
    void shouldRejectUnknownSortField() {
        assertThatThrownBy(() -> parser.parse("desconhecido asc", representation))
                .isInstanceOf(RepresentationException.class)
                .hasMessageContaining("desconhecido");
    }

    @Test
    void shouldRejectInvalidDirection() {
        assertThatThrownBy(() -> parser.parse("nome sideways", representation))
                .isInstanceOf(RepresentationException.class)
                .hasMessageContaining("sideways");
    }
}
