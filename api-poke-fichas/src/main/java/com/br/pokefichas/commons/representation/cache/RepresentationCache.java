package com.br.pokefichas.commons.representation.cache;

import com.br.pokefichas.commons.representation.model.Representation;
import com.br.pokefichas.commons.representation.model.RepresentationProvider;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RepresentationCache {

    @SuppressWarnings("rawtypes")
    private final Map<Class<? extends RepresentationProvider>, Representation> cache = new ConcurrentHashMap<>();

    @SuppressWarnings({"unchecked", "rawtypes"})
    public Representation<?, ?> get(final Class<? extends RepresentationProvider<?, ?>> providerClass) {
        return cache.computeIfAbsent((Class<? extends RepresentationProvider>) providerClass, this::resolve);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Representation<?, ?> resolve(final Class<? extends RepresentationProvider> providerClass) {
        try {
            final RepresentationProvider<?, ?> provider =
                    (RepresentationProvider<?, ?>) providerClass.getDeclaredConstructor().newInstance();
            return provider.getRepresentation();
        } catch (final Exception e) {
            throw new IllegalStateException(
                    "Falha ao instanciar RepresentationProvider: " + providerClass.getName(), e);
        }
    }
}
