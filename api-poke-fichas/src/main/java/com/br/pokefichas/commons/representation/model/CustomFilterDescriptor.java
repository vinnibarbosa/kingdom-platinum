package com.br.pokefichas.commons.representation.model;

import com.querydsl.core.types.Predicate;

import java.util.function.Function;

public final class CustomFilterDescriptor<T> {

    private final String alias;
    private final Class<T> type;
    private final Function<T, Predicate> predicateFactory;

    public CustomFilterDescriptor(final String alias,
                                  final Class<T> type,
                                  final Function<T, Predicate> predicateFactory) {
        this.alias = alias;
        this.type = type;
        this.predicateFactory = predicateFactory;
    }

    public String getAlias() { return alias; }
    public Class<T> getType() { return type; }

    public Predicate resolve(final T value) {
        return predicateFactory.apply(value);
    }
}
