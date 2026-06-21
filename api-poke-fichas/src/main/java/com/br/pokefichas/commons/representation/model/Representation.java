package com.br.pokefichas.commons.representation.model;

import com.br.pokefichas.commons.page.Sort;
import com.querydsl.core.types.dsl.SimpleExpression;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Representation<O, I> {

    private final Class<O> outputClass;
    private final Class<I> inputClass;
    private final Map<String, FieldDescriptor<?>> fields;
    private final Map<String, SimpleExpression<?>> filterExpressions;
    private final Map<String, SimpleExpression<?>> sortExpressions;
    private final Map<String, CustomFilterDescriptor<?>> customFilters;
    private final Sort defaultSort;

    public Representation(final Class<O> outputClass,
                          final Class<I> inputClass,
                          final Map<String, FieldDescriptor<?>> fields,
                          final Map<String, SimpleExpression<?>> filterExpressions,
                          final Map<String, SimpleExpression<?>> sortExpressions,
                          final Map<String, CustomFilterDescriptor<?>> customFilters,
                          final Sort defaultSort) {
        this.outputClass = outputClass;
        this.inputClass = inputClass;
        this.fields = Collections.unmodifiableMap(new LinkedHashMap<>(fields));
        this.filterExpressions = Collections.unmodifiableMap(new LinkedHashMap<>(filterExpressions));
        this.sortExpressions = Collections.unmodifiableMap(new LinkedHashMap<>(sortExpressions));
        this.customFilters = Collections.unmodifiableMap(new LinkedHashMap<>(customFilters));
        this.defaultSort = defaultSort;
    }

    public Class<O> getOutputClass() { return outputClass; }
    public Class<I> getInputClass() { return inputClass; }
    public Map<String, FieldDescriptor<?>> getFields() { return fields; }
    public Map<String, SimpleExpression<?>> getFilterExpressions() { return filterExpressions; }
    public Map<String, SimpleExpression<?>> getSortExpressions() { return sortExpressions; }
    public Map<String, CustomFilterDescriptor<?>> getCustomFilters() { return customFilters; }
    public Sort getDefaultSort() { return defaultSort; }

    public boolean isFilterable(final String alias) {
        return filterExpressions.containsKey(alias) || customFilters.containsKey(alias);
    }

    public boolean isSortable(final String alias) {
        return sortExpressions.containsKey(alias);
    }

    public SimpleExpression<?> filterExpression(final String alias) {
        return filterExpressions.get(alias);
    }

    public CustomFilterDescriptor<?> customFilter(final String alias) {
        return customFilters.get(alias);
    }

    public SimpleExpression<?> sortExpression(final String alias) {
        return sortExpressions.get(alias);
    }

    public boolean hasProjectionFields() {
        return fields.values().stream().anyMatch(FieldDescriptor::isProjection);
    }
}
