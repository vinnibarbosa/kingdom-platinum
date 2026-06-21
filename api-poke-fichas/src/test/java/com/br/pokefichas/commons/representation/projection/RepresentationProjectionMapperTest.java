package com.br.pokefichas.commons.representation.projection;

import com.br.pokefichas.commons.representation.RepresentationBuilder;
import com.br.pokefichas.commons.representation.model.Representation;
import com.querydsl.core.types.FactoryExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RepresentationProjectionMapperTest {

    private final RepresentationProjectionMapper mapper = new RepresentationProjectionMapper();

    @Test
    void shouldCreateConstructorProjectionFromRepresentationFields() {
        final NumberPath<Long> id = Expressions.numberPath(Long.class, "id");
        final StringPath nome = Expressions.stringPath("nome");
        final Representation<TestResponse, Object> representation =
                RepresentationBuilder.create(TestResponse.class, Object.class)
                        .identifier(id).add()
                        .filterableAndSortable("nome", nome)
                        .readOnly()
                        .build();

        final FactoryExpression<TestResponse> projection = mapper.constructorProjection(representation);

        assertThat(projection.getType()).isEqualTo(TestResponse.class);
        assertThat(projection.getArgs()).containsExactly(id, nome);
    }

    public record TestResponse(Long id, String nome) {
    }
}
