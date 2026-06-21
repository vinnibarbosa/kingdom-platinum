package com.br.pokefichas.commons.representation.parser;

import com.br.pokefichas.commons.page.Sort;
import com.br.pokefichas.commons.representation.exception.RepresentationException;
import com.br.pokefichas.commons.representation.model.Representation;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.SimpleExpression;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SortParser {

    public Sort parse(final String rawSort, final Representation<?, ?> representation) {
        if (rawSort == null || rawSort.isBlank()) {
            return representation.getDefaultSort();
        }

        final List<OrderSpecifier<?>> orders = new ArrayList<>();
        for (final String part : rawSort.split(",")) {
            final String trimmed = part.trim();
            if (trimmed.isEmpty()) {
                continue;
            }

            final String[] tokens = trimmed.split("\\s+");
            if (tokens.length > 2) {
                throw new RepresentationException(
                        "Expressao de ordenacao invalida '" + trimmed + "' — formato esperado: 'campo [asc|desc]'");
            }
            final String alias = tokens[0].trim();
            final String direction = tokens.length > 1 ? tokens[1].trim().toLowerCase() : "asc";

            final SimpleExpression<?> expr = representation.sortExpression(alias);
            if (expr == null) {
                throw new RepresentationException(
                        "Campo '" + alias + "' nao e permitido na ordenacao desta representacao");
            }
            if (!"asc".equals(direction) && !"desc".equals(direction)) {
                throw new RepresentationException(
                        "Direcao de ordenacao invalida '" + direction + "' — use 'asc' ou 'desc'");
            }
            if (!(expr instanceof ComparableExpressionBase<?> comparable)) {
                throw new RepresentationException(
                        "Campo '" + alias + "' nao suporta ordenacao");
            }

            orders.add("desc".equals(direction) ? comparable.desc() : comparable.asc());
        }

        if (orders.isEmpty()) {
            return representation.getDefaultSort();
        }

        return Sort.of(orders);
    }
}
