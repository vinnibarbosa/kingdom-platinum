package com.br.pokefichas.commons.representation;

import com.br.pokefichas.commons.page.Sort;
import com.br.pokefichas.commons.representation.model.CustomFilterDescriptor;
import com.br.pokefichas.commons.representation.model.FieldDescriptor;
import com.br.pokefichas.commons.representation.model.Representation;
import com.br.pokefichas.commons.representation.model.RepresentationProvider;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.ListPath;
import com.querydsl.core.types.dsl.SimpleExpression;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public final class RepresentationBuilder<O, I> {

    private final Class<O> outputClass;
    private final Class<I> inputClass;
    private final Map<String, FieldDescriptor<?>> fields = new LinkedHashMap<>();
    private final Map<String, SimpleExpression<?>> filterExpressions = new LinkedHashMap<>();
    private final Map<String, SimpleExpression<?>> sortExpressions = new LinkedHashMap<>();
    private final Map<String, CustomFilterDescriptor<?>> customFilters = new LinkedHashMap<>();
    private Sort defaultSort;
    private boolean readOnly;

    private RepresentationBuilder(final Class<O> outputClass, final Class<I> inputClass) {
        this.outputClass = outputClass;
        this.inputClass = inputClass;
    }

    public static <O, I> RepresentationBuilder<O, I> create(final Class<O> outputClass, final Class<I> inputClass) {
        return new RepresentationBuilder<>(outputClass, inputClass);
    }

    public <T> FieldStep<T, O, I> identifier(final Path<T> path) {
        return new FieldStep<>(this, path, true);
    }

    public <T> FieldStep<T, O, I> field(final Path<T> path) {
        return new FieldStep<>(this, path, false);
    }

    public <T> CollectionStep<T, O, I> collection(final ListPath<T, ?> path) {
        return new CollectionStep<>(this, path);
    }

    public RepresentationBuilder<O, I> filterOnly(final String alias, final SimpleExpression<?> expr) {
        registerExpression(alias, expr, true, false, false);
        return this;
    }

    public <T> RepresentationBuilder<O, I> customFilter(final String alias,
                                                        final Class<T> type,
                                                        final Function<T, Predicate> predicateFactory) {
        customFilters.put(alias, new CustomFilterDescriptor<>(alias, type, predicateFactory));
        return this;
    }

    public RepresentationBuilder<O, I> filterable(final String alias, final SimpleExpression<?> expr) {
        registerExpression(alias, expr, true, false, true);
        return this;
    }

    public RepresentationBuilder<O, I> sortable(final String alias, final SimpleExpression<?> expr) {
        registerExpression(alias, expr, false, true, true);
        return this;
    }

    public RepresentationBuilder<O, I> filterableAndSortable(final String alias, final SimpleExpression<?> expr) {
        registerExpression(alias, expr, true, true, true);
        return this;
    }

    public RepresentationBuilder<O, I> defaultSort(final Sort sort) {
        this.defaultSort = sort;
        return this;
    }

    public RepresentationBuilder<O, I> readOnly() {
        this.readOnly = true;
        fields.replaceAll((alias, descriptor) -> descriptor.asReadonly());
        return this;
    }

    public Representation<O, I> build() {
        return new Representation<>(
                outputClass,
                inputClass,
                fields,
                filterExpressions,
                sortExpressions,
                customFilters,
                defaultSort
        );
    }

    @SuppressWarnings("unchecked")
    <T> void registerField(final String alias, final Class<?> type, final SimpleExpression<?> expression,
                           final boolean identifier, final boolean filterable, final boolean sortable,
                           final boolean projection, final Class<?> configurationProvider) {
        fields.put(alias, new FieldDescriptor<>(alias, (Class<T>) type, identifier,
                this.readOnly, false, false, (SimpleExpression<T>) expression,
                (SimpleExpression<T>) expression, projection, null, configurationProvider));
        if (filterable) {
            filterExpressions.put(alias, expression);
        }
        if (sortable) {
            sortExpressions.put(alias, expression);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void registerExpression(final String alias, final SimpleExpression<?> expression,
                                        final boolean filterable, final boolean sortable,
                                        final boolean projection) {
        fields.putIfAbsent(alias, new FieldDescriptor<>(
                alias,
                (Class<T>) expression.getType(),
                false,
                readOnly,
                false,
                false,
                (SimpleExpression<T>) expression,
                (SimpleExpression<T>) expression,
                projection,
                null,
                null
        ));
        if (filterable) {
            filterExpressions.put(alias, expression);
        }
        if (sortable) {
            sortExpressions.put(alias, expression);
        }
    }

    public static final class FieldStep<T, O, I> {

        private final RepresentationBuilder<O, I> parent;
        private final Path<T> path;
        private final boolean identifier;
        private boolean filterable;
        private boolean sortable;
        private boolean projection = true;
        private Class<?> configurationProvider;

        FieldStep(final RepresentationBuilder<O, I> parent, final Path<T> path, final boolean identifier) {
            this.parent = parent;
            this.path = path;
            this.identifier = identifier;
        }

        public FieldStep<T, O, I> filterable() {
            this.filterable = true;
            return this;
        }

        public FieldStep<T, O, I> sortable() {
            this.sortable = true;
            return this;
        }

        public FieldStep<T, O, I> notProjected() {
            this.projection = false;
            return this;
        }

        public FieldStep<T, O, I> configuration(final Class<? extends RepresentationProvider<T, ?>> providerClass) {
            this.configurationProvider = providerClass;
            return this;
        }

        @SuppressWarnings("unchecked")
        public RepresentationBuilder<O, I> add() {
            final String alias = path.getMetadata().getName();
            final Class<?> type = path.getType();
            final SimpleExpression<T> expr = (SimpleExpression<T>) path;
            parent.registerField(alias, type, expr, identifier, filterable, sortable, projection, configurationProvider);
            return parent;
        }
    }

    public static final class CollectionStep<T, O, I> {

        private final RepresentationBuilder<O, I> parent;
        private final ListPath<T, ?> path;
        private Class<?> configurationProvider;

        CollectionStep(final RepresentationBuilder<O, I> parent, final ListPath<T, ?> path) {
            this.parent = parent;
            this.path = path;
        }

        public CollectionStep<T, O, I> configuration(final Class<? extends RepresentationProvider<T, ?>> providerClass) {
            this.configurationProvider = providerClass;
            return this;
        }

        @SuppressWarnings("unchecked")
        public RepresentationBuilder<O, I> add() {
            final String alias = path.getMetadata().getName();
            parent.fields.put(alias, new FieldDescriptor<>(
                    alias,
                    (Class<T>) path.getElementType(),
                    false,
                    parent.readOnly,
                    true,
                    false,
                    null,
                    null,
                    false,
                    path.getType(),
                    configurationProvider
            ));
            return parent;
        }
    }
}
