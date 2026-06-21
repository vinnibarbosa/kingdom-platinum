package com.br.pokefichas.commons.representation.annotation;

import com.br.pokefichas.commons.representation.model.RepresentationProvider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Representation {

    Class<? extends RepresentationProvider<?, ?>> value();
}
