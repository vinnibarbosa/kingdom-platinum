package com.br.pokefichas.commons.page;

import java.util.Objects;

public final class Pageable {

    public static final int DEFAULT_LIMIT = 25;
    public static final int MAX_LIMIT = 1000;

    private final int offset;
    private final int limit;
    private final Sort sort;
    private final boolean skipCount;

    private Pageable(final int offset, final int limit, final Sort sort, final boolean skipCount) {
        if (offset < 0) {
            throw new IllegalArgumentException("Offset must not be less than zero!");
        }
        if (limit < 0) {
            throw new IllegalArgumentException("Limit must not be less than zero!");
        }
        if (limit > MAX_LIMIT) {
            throw new IllegalArgumentException("Limit must not be greater than " + MAX_LIMIT + "!");
        }

        if (limit == 0) {
            this.limit = DEFAULT_LIMIT;
            this.offset = offset;
            this.sort = sort;
            this.skipCount = skipCount;
            return;
        }

        this.offset = offset;
        this.limit = limit;
        this.sort = sort;
        this.skipCount = skipCount;
    }

    public static Pageable of(final int offset, final int limit) {
        return of(offset, limit, null);
    }

    public static Pageable of(final int offset, final int limit, final Sort sort) {
        return new Pageable(offset, limit, sort, false);
    }

    public static Pageable of(final int offset, final int limit, final Sort sort, final boolean skipCount) {
        return new Pageable(offset, limit, sort, skipCount);
    }

    public int getLimit() { return limit; }
    public int getOffset() { return offset; }
    public Sort getSort() { return sort; }
    public boolean isSkipCount() { return skipCount; }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Pageable)) {
            return false;
        }
        final Pageable that = (Pageable) obj;
        return this.offset == that.offset &&
               this.limit == that.limit &&
               this.skipCount == that.skipCount &&
               Objects.equals(this.sort, that.sort);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + this.offset;
        hash = 53 * hash + this.limit;
        hash = 53 * hash + Boolean.hashCode(this.skipCount);
        hash = 53 * hash + Objects.hashCode(this.sort);
        return hash;
    }
}
