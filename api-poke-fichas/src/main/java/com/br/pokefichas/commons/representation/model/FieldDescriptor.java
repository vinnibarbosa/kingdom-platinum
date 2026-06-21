package com.br.pokefichas.commons.representation.model;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.SimpleExpression;

public final class FieldDescriptor<T> {

    private final String name;
    private final Class<T> type;
    private final boolean identifier;
    private final boolean readonly;
    private final boolean collection;
    private final boolean map;
    private final SimpleExpression<T> expression;
    private final Expression<T> readExpression;
    private final boolean projection;
    private final Class<?> collectionType;
    private final Class<?> configurationProvider;

    public FieldDescriptor(final String name, final Class<T> type, final boolean identifier, final boolean readonly,
                           final boolean collection, final boolean map, final SimpleExpression<T> expression) {
        this(name, type, identifier, readonly, collection, map, expression, expression, true, null, null);
    }

    public FieldDescriptor(final String name, final Class<T> type, final boolean identifier, final boolean readonly,
                           final boolean collection, final boolean map, final SimpleExpression<T> expression,
                           final Expression<T> readExpression, final boolean projection) {
        this(name, type, identifier, readonly, collection, map, expression, readExpression, projection, null, null);
    }

    public FieldDescriptor(final String name, final Class<T> type, final boolean identifier, final boolean readonly,
                           final boolean collection, final boolean map, final SimpleExpression<T> expression,
                           final Expression<T> readExpression, final boolean projection,
                           final Class<?> collectionType, final Class<?> configurationProvider) {
        this.name = name;
        this.type = type;
        this.identifier = identifier;
        this.readonly = readonly;
        this.collection = collection;
        this.map = map;
        this.expression = expression;
        this.readExpression = readExpression;
        this.projection = projection;
        this.collectionType = collectionType;
        this.configurationProvider = configurationProvider;
    }

    public String getName() { return name; }
    public Class<T> getType() { return type; }
    public boolean isIdentifier() { return identifier; }
    public boolean isReadonly() { return readonly; }
    public boolean isCollection() { return collection; }
    public boolean isMap() { return map; }
    public SimpleExpression<T> getExpression() { return expression; }
    public Expression<T> getReadExpression() { return readExpression; }
    public boolean isProjection() { return projection; }
    public Class<?> getCollectionType() { return collectionType; }
    public Class<?> getConfigurationProvider() { return configurationProvider; }

    public FieldDescriptor<T> asReadonly() {
        if (readonly) {
            return this;
        }
        return new FieldDescriptor<>(
                name,
                type,
                identifier,
                true,
                collection,
                map,
                expression,
                readExpression,
                projection,
                collectionType,
                configurationProvider
        );
    }
}
