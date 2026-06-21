package com.br.pokefichas.commons.builder;

public interface IBuilder<T> {
    T build();

    default T build(boolean validateBusiness) {
        return build();
    }
}
