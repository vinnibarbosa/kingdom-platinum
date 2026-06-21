package com.br.pokefichas.commons.representation.projection;

import com.br.pokefichas.commons.representation.exception.RepresentationException;
import com.br.pokefichas.commons.representation.model.FieldDescriptor;
import com.br.pokefichas.commons.representation.model.Representation;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.FactoryExpression;
import com.querydsl.core.types.Projections;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RepresentationProjectionMapper {

    public <O, I> FactoryExpression<O> constructorProjection(final Representation<O, I> representation) {
        final List<Expression<?>> expressions = new ArrayList<>();
        for (final FieldDescriptor<?> descriptor : representation.getFields().values()) {
            if (descriptor.isProjection()) {
                expressions.add(descriptor.getReadExpression());
            }
        }

        if (expressions.isEmpty()) {
            throw new RepresentationException("Representacao nao possui fields projetaveis");
        }

        return Projections.constructor(
                representation.getOutputClass(),
                expressions.toArray(Expression<?>[]::new)
        );
    }
}
