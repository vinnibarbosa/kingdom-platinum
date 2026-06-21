package com.br.pokefichas.commons.page;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Function;

public class Page<T> implements Iterable<T> {

    private final Collection<T> content;
    private final Pageable pageable;
    private final long total;

    public Page(final Collection<T> content, final Pageable pageable, long total) {
        Objects.requireNonNull(content);
        Objects.requireNonNull(pageable);
        this.content = content;
        this.pageable = pageable;
        this.total = total;
    }

    public static <T> Page<T> empty() {
        return new Page<>(Collections.emptyList(), Pageable.of(0, 0), 0);
    }

    public <U> Page<U> map(final Function<T, U> mapper) {
        Objects.requireNonNull(mapper, "Mapper function cannot be null");
        final Collection<U> mappedContent = content.stream().map(mapper).toList();
        return new Page<>(mappedContent, pageable, total);
    }

    public Pageable getPageable() { return pageable; }
    public Collection<T> getContent() { return Collections.unmodifiableCollection(content); }
    public long getTotal() { return total; }

    @Override
    public Iterator<T> iterator() {
        return content.iterator();
    }
}
