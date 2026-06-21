package com.br.pokefichas.commons.representation.parser;

import com.br.pokefichas.commons.representation.exception.RepresentationException;
import com.br.pokefichas.commons.representation.model.CustomFilterDescriptor;
import com.br.pokefichas.commons.representation.model.Representation;
import com.br.pokefichas.commons.representation.parser.FilterToken.FilterTokenType;
import com.br.pokefichas.commons.representation.resolver.ExpressionOperationResolver;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.SimpleExpression;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class FilterParser {

    private final ExpressionOperationResolver resolver;

    public FilterParser(final ExpressionOperationResolver resolver) {
        this.resolver = resolver;
    }

    public BooleanBuilder parse(final String rawFilter, final Representation<?, ?> representation) {
        if (rawFilter == null || rawFilter.isBlank()) {
            return new BooleanBuilder();
        }
        final List<FilterToken> tokens = new FilterLexer(rawFilter.trim()).tokenize();
        return new ParseContext(tokens, representation, resolver).parse();
    }

    private static final class ParseContext {

        private final List<FilterToken> tokens;
        private final Representation<?, ?> representation;
        private final ExpressionOperationResolver resolver;
        private int pos;

        ParseContext(final List<FilterToken> tokens, final Representation<?, ?> representation,
                     final ExpressionOperationResolver resolver) {
            this.tokens = tokens;
            this.representation = representation;
            this.resolver = resolver;
            this.pos = 0;
        }

        BooleanBuilder parse() {
            final BooleanBuilder builder = new BooleanBuilder();
            builder.and(parseOrExpr());
            expect(FilterTokenType.EOF);
            return builder;
        }

        private Predicate parseOrExpr() {
            Predicate left = parseAndExpr();
            while (current().type() == FilterTokenType.OR) {
                advance();
                final Predicate right = parseAndExpr();
                left = new BooleanBuilder(left).or(right);
            }
            return left;
        }

        private Predicate parseAndExpr() {
            Predicate left = parsePrimary();
            while (current().type() == FilterTokenType.AND) {
                advance();
                final Predicate right = parsePrimary();
                left = new BooleanBuilder(left).and(right);
            }
            return left;
        }

        private Predicate parsePrimary() {
            if (current().type() == FilterTokenType.LPAREN) {
                advance();
                final Predicate inner = parseOrExpr();
                expect(FilterTokenType.RPAREN);
                return inner;
            }
            return parseComparison();
        }

        private Predicate parseComparison() {
            final FilterToken identToken = expect(FilterTokenType.IDENTIFIER);
            final String alias = identToken.value();

            final CustomFilterDescriptor<?> customFilter = representation.customFilter(alias);
            if (customFilter != null) {
                final String operator = parseOperator();
                final String rawValue = parseScalarValue();
                return resolver.resolveCustom(customFilter, operator, rawValue);
            }

            final SimpleExpression<?> expression = representation.filterExpression(alias);
            if (expression == null) {
                throw new RepresentationException(
                        "Campo '" + alias + "' nao e permitido no filtro desta representacao");
            }

            if (current().type() == FilterTokenType.IN) {
                advance();
                final List<String> rawValues = parseInList();
                return resolver.resolveIn(expression, rawValues);
            }

            final String operator = parseOperator();
            final String rawValue = parseScalarValue();
            return resolver.resolve(expression, operator, rawValue);
        }

        private String parseOperator() {
            final FilterToken token = current();
            return switch (token.type()) {
                case EQ -> { advance(); yield "="; }
                case NEQ -> { advance(); yield "!="; }
                case GT -> { advance(); yield ">"; }
                case GTE -> { advance(); yield ">="; }
                case LT -> { advance(); yield "<"; }
                case LTE -> { advance(); yield "<="; }
                case LIKE -> { advance(); yield "like"; }
                default -> throw new RepresentationException(
                        "Operador invalido '" + token.value() + "' na posicao " + token.position());
            };
        }

        private String parseScalarValue() {
            final FilterToken token = current();
            return switch (token.type()) {
                case STRING, NUMBER, IDENTIFIER -> { advance(); yield token.value(); }
                default -> throw new RepresentationException(
                        "Valor esperado mas encontrado '" + token.value() + "' na posicao " + token.position());
            };
        }

        private List<String> parseInList() {
            expect(FilterTokenType.LPAREN);
            final List<String> values = new ArrayList<>();
            values.add(parseScalarValue());
            while (current().type() == FilterTokenType.COMMA) {
                advance();
                values.add(parseScalarValue());
            }
            expect(FilterTokenType.RPAREN);
            return values;
        }

        private FilterToken current() {
            return tokens.get(pos);
        }

        private void advance() {
            if (pos < tokens.size() - 1) {
                pos++;
            }
        }

        private FilterToken expect(final FilterTokenType type) {
            final FilterToken token = current();
            if (token.type() != type) {
                throw new RepresentationException(
                        "Esperado " + type + " mas encontrado '" + token.value()
                        + "' na posicao " + token.position());
            }
            advance();
            return token;
        }
    }
}
