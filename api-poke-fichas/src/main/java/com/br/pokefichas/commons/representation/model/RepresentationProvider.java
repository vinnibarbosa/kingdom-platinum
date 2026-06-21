package com.br.pokefichas.commons.representation.model;

public interface RepresentationProvider<O, I> {

    Representation<O, I> getRepresentation();
}
