package com.br.pokefichas.commons.representation.annotation;

import com.br.pokefichas.commons.representation.model.RepresentationProvider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface RepresentationSort {

    Class<? extends RepresentationProvider<?, ?>> provider() default DefaultProvider.class;

    String param() default "sort";

    final class DefaultProvider implements RepresentationProvider<Void, Void> {
        @Override
        public com.br.pokefichas.commons.representation.model.Representation<Void, Void> getRepresentation() {
            throw new UnsupportedOperationException("Sentinel provider — use method-level @Representation");
        }
    }
}
