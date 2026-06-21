package com.br.pokefichas.commons.utils;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

public final class ObjectUtil {

    public static <T, R> R getIfExists(final T object, final Function<T, R> function) {
        return getIfExists(object, function, null);
    }

    public static <T, R> R getIfExists(final T object, final R defaultValue) {
        return getIfExists(object, null, defaultValue);
    }

    @SuppressWarnings("unchecked")
    public static <T, R> R getIfExists(final T object, final Function<T, R> function, final R defaultValue) {
        return switch (object) {
            case null -> defaultValue;
            case Object obj when function == null -> (R) obj;
            case Object obj -> {
                R result = function.apply((T) obj);
                yield isNull(result) ? defaultValue : result;
            }
        };
    }

    public static <T extends Collection<?>, R> R getIfExists(final T collection, final Function<T, R> function) {
        return isEmpty(collection) ? null : function.apply(collection);
    }

    public static <T> void setIfExists(final T object, final Consumer<T> consumer) {
        if (nonNull(object)) {
            consumer.accept(object);
        }
    }

    public static <T> void doIfExists(final T object, final Consumer<T> consumer) {
        if (nonNull(object)) {
            consumer.accept(object);
        }
    }
}