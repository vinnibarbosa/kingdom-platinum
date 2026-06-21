package com.br.pokefichas.commons.representation.parser;

import com.br.pokefichas.commons.representation.RepresentationBuilder;
import com.br.pokefichas.commons.representation.exception.RepresentationException;
import com.br.pokefichas.commons.representation.model.Representation;
import com.br.pokefichas.commons.representation.resolver.ExpressionOperationResolver;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FilterParserTest {

    private FilterParser parser;
    private Representation<?, ?> representation;

    @BeforeEach
    void setUp() {
        parser = new FilterParser(new ExpressionOperationResolver());

        final StringPath nome = Expressions.stringPath("nome");
        final NumberPath<Long> id = Expressions.numberPath(Long.class, "id");
        final StringPath status = Expressions.stringPath("status");

        representation = RepresentationBuilder.create(Object.class, Object.class)
                .filterableAndSortable("nome", nome)
                .filterableAndSortable("id", id)
                .filterable("status", status)
                .customFilter("nomeCustomizado", String.class, value -> nome.eq(value + "-custom"))
                .build();
    }

    @Test
    void shouldReturnEmptyBuilderForNullInput() {
        final BooleanBuilder result = parser.parse(null, representation);
        assertThat(result.hasValue()).isFalse();
    }

    @Test
    void shouldReturnEmptyBuilderForBlankInput() {
        final BooleanBuilder result = parser.parse("  ", representation);
        assertThat(result.hasValue()).isFalse();
    }

    @Test
    void shouldParseEqualityFilter() {
        final BooleanBuilder result = parser.parse("nome = \"Empresa XPTO\"", representation);
        assertThat(result.hasValue()).isTrue();
        assertThat(result.toString()).containsIgnoringCase("nome");
    }

    @Test
    void shouldParseInequalityFilter() {
        final BooleanBuilder result = parser.parse("nome != \"ignorado\"", representation);
        assertThat(result.hasValue()).isTrue();
    }

    @Test
    void shouldParseNumericEquality() {
        final BooleanBuilder result = parser.parse("id = 42", representation);
        assertThat(result.hasValue()).isTrue();
    }

    @Test
    void shouldParseInOperatorWithNumbers() {
        final BooleanBuilder result = parser.parse("id in (1, 2, 3)", representation);
        assertThat(result.hasValue()).isTrue();
    }

    @Test
    void shouldParseGreaterThan() {
        final BooleanBuilder result = parser.parse("id > 100", representation);
        assertThat(result.hasValue()).isTrue();
    }

    @Test
    void shouldParseAndExpression() {
        final BooleanBuilder result = parser.parse("id = 1 and nome = \"Teste\"", representation);
        assertThat(result.hasValue()).isTrue();
    }

    @Test
    void shouldParseOrExpression() {
        final BooleanBuilder result = parser.parse("id = 1 or id = 2", representation);
        assertThat(result.hasValue()).isTrue();
    }

    @Test
    void shouldParseGroupedExpressionWithParentheses() {
        final BooleanBuilder result = parser.parse("(id = 1 or id = 2) and nome = \"X\"", representation);
        assertThat(result.hasValue()).isTrue();
    }

    @Test
    void shouldParseLikeFilter() {
        final BooleanBuilder result = parser.parse("nome like \"empresa\"", representation);
        assertThat(result.hasValue()).isTrue();
    }

    @Test
    void shouldParseLikeWithPercent() {
        final BooleanBuilder result = parser.parse("nome like \"emp%\"", representation);
        assertThat(result.hasValue()).isTrue();
    }

    @Test
    void shouldRejectUnknownField() {
        assertThatThrownBy(() -> parser.parse("desconhecido = \"valor\"", representation))
                .isInstanceOf(RepresentationException.class)
                .hasMessageContaining("desconhecido");
    }

    @Test
    void shouldRejectInvalidOperator() {
        assertThatThrownBy(() -> parser.parse("id and 1", representation))
                .isInstanceOf(RepresentationException.class);
    }

    @Test
    void shouldParseComplexNestedFilter() {
        final BooleanBuilder result = parser.parse(
                "(id in (1, 2) and nome = \"Teste\") or status = \"ATIVO\"", representation);
        assertThat(result.hasValue()).isTrue();
    }

    @Test
    void shouldParseCustomFilter() {
        final BooleanBuilder result = parser.parse("nomeCustomizado = \"Teste\"", representation);
        assertThat(result.hasValue()).isTrue();
        assertThat(result.toString()).contains("Teste-custom");
    }

    @Test
    void shouldRejectUnsupportedOperatorForCustomFilter() {
        assertThatThrownBy(() -> parser.parse("nomeCustomizado != \"Teste\"", representation))
                .isInstanceOf(RepresentationException.class)
                .hasMessageContaining("filtro customizado");
    }
}
