package com.br.pokefichas.commons.page;

import com.google.common.base.Joiner;
import com.querydsl.core.types.OrderSpecifier;

import java.io.Serializable;
import java.util.*;

public final class Sort implements Iterable<OrderSpecifier<?>>, Serializable {

    private final List<OrderSpecifier<?>> orders;

    private Sort(final List<OrderSpecifier<?>> orders) {
        this.orders = Objects.nonNull(orders) ? orders : Collections.emptyList();
    }

    public static Sort of(final OrderSpecifier<?>... orders) {
        return of(Arrays.asList(orders));
    }

    public static Sort of(final List<OrderSpecifier<?>> orders) {
        return new Sort(orders);
    }

    public static Sort of(final Iterable<OrderSpecifier<?>> orders) {
        final List<OrderSpecifier<?>> orderList = new ArrayList<>();
        for (final OrderSpecifier<?> order : orders) {
            orderList.add(order);
        }
        return of(orderList);
    }

    public Sort and(final Sort sort) {
        if (sort == null) {
            return this;
        }
        final List<OrderSpecifier<?>> combined = new ArrayList<>(this.orders);
        for (final OrderSpecifier<?> order : sort.orders) {
            combined.add(order);
        }
        return of(combined);
    }

    public boolean isEmpty() {
        return this.orders.isEmpty();
    }

    @Override
    public Iterator<OrderSpecifier<?>> iterator() {
        return this.orders.iterator();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Sort)) {
            return false;
        }
        final Sort that = (Sort) obj;
        return this.orders.equals(that.orders);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + orders.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return Joiner.on(", ").join(orders);
    }
}
