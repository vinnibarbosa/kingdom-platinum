package com.br.pokefichas.commons.representation;

import com.br.pokefichas.commons.representation.model.FieldDescriptor;
import com.br.pokefichas.commons.representation.model.Representation;
import com.br.pokefichas.commons.representation.model.RepresentationProvider;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.ListPath;
import com.querydsl.core.types.dsl.SimplePath;
import com.querydsl.core.types.dsl.StringPath;
import org.junit.jupiter.api.Test;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;
import static org.assertj.core.api.Assertions.assertThat;

class RepresentationBuilderTest {

    @Test
    void shouldRegisterFieldDescriptorForExpressionShortcuts() {
        final StringPath nome = Expressions.stringPath("nome");

        final Representation<Object, Object> representation = RepresentationBuilder.create(Object.class, Object.class)
                .filterableAndSortable("nome", nome)
                .build();

        assertThat(representation.getFields()).containsKey("nome");
        assertThat(representation.isFilterable("nome")).isTrue();
        assertThat(representation.isSortable("nome")).isTrue();
    }

    @Test
    void shouldMarkAlreadyRegisteredFieldsAsReadonly() {
        final StringPath nome = Expressions.stringPath("nome");

        final Representation<Object, Object> representation = RepresentationBuilder.create(Object.class, Object.class)
                .filterableAndSortable("nome", nome)
                .readOnly()
                .build();

        final FieldDescriptor<?> descriptor = representation.getFields().get("nome");
        assertThat(descriptor.isReadonly()).isTrue();
    }

    @Test
    void shouldRegisterFilterOnlyExpressionWithoutProjection() {
        final StringPath codigoProduto = Expressions.stringPath("codigoProduto");

        final Representation<Object, Object> representation = RepresentationBuilder.create(Object.class, Object.class)
                .filterOnly("itemCodigoProduto", codigoProduto)
                .build();

        final FieldDescriptor<?> descriptor = representation.getFields().get("itemCodigoProduto");
        assertThat(representation.isFilterable("itemCodigoProduto")).isTrue();
        assertThat(representation.isSortable("itemCodigoProduto")).isFalse();
        assertThat(descriptor.isProjection()).isFalse();
    }

    @Test
    void shouldRegisterCustomFilterWithoutProjectionField() {
        final Representation<Object, Object> representation = RepresentationBuilder.create(Object.class, Object.class)
                .customFilter("existsItem", Boolean.class, value -> Expressions.TRUE)
                .build();

        assertThat(representation.isFilterable("existsItem")).isTrue();
        assertThat(representation.getCustomFilters()).containsKey("existsItem");
        assertThat(representation.getFields()).doesNotContainKey("existsItem");
    }

    @Test
    void shouldRegisterCollectionConfigurationMetadata() {
        final ListPath<TestItem, ?> itens = Expressions.listPath(
                TestItem.class,
                SimplePath.class,
                forVariable("itens")
        );

        final Representation<Object, Object> representation = RepresentationBuilder.create(Object.class, Object.class)
                .collection(itens).configuration(TestItemRepresentation.class).add()
                .build();

        final FieldDescriptor<?> descriptor = representation.getFields().get("itens");
        assertThat(descriptor.isCollection()).isTrue();
        assertThat(descriptor.isProjection()).isFalse();
        assertThat(descriptor.getType()).isEqualTo(TestItem.class);
        assertThat(descriptor.getConfigurationProvider()).isEqualTo(TestItemRepresentation.class);
    }

    private static final class TestItem {
    }

    public static final class TestItemRepresentation implements RepresentationProvider<TestItem, Object> {

        @Override
        public Representation<TestItem, Object> getRepresentation() {
            return RepresentationBuilder.create(TestItem.class, Object.class).build();
        }
    }
}
