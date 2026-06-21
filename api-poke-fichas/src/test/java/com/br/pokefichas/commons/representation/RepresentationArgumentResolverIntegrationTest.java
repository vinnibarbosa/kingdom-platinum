package com.br.pokefichas.commons.representation;

import com.br.pokefichas.commons.page.Sort;
import com.br.pokefichas.commons.representation.annotation.RepresentationFilter;
import com.br.pokefichas.commons.representation.annotation.RepresentationSort;
import com.br.pokefichas.commons.representation.cache.RepresentationCache;
import com.br.pokefichas.commons.representation.model.Representation;
import com.br.pokefichas.commons.representation.model.RepresentationProvider;
import com.br.pokefichas.commons.representation.parser.FilterParser;
import com.br.pokefichas.commons.representation.parser.SortParser;
import com.br.pokefichas.commons.representation.resolver.ExpressionOperationResolver;
import com.br.pokefichas.commons.representation.web.RepresentationFilterResolver;
import com.br.pokefichas.commons.representation.web.RepresentationSortResolver;
import com.br.pokefichas.infra.web.GlobalExceptionHandler;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RepresentationArgumentResolverIntegrationTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        final RepresentationMapper mapper = new RepresentationMapper(
                new RepresentationCache(),
                new FilterParser(new ExpressionOperationResolver()),
                new SortParser()
        );

        mockMvc = MockMvcBuilders.standaloneSetup(new SampleRepresentationController())
                .setCustomArgumentResolvers(
                        new RepresentationFilterResolver(mapper),
                        new RepresentationSortResolver(mapper)
                )
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldResolveBooleanBuilderFromFilterQueryParam() throws Exception {
        mockMvc.perform(get("/test/representation")
                        .param("filter", "id = 1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("filter=true")));
    }

    @Test
    void shouldResolveSortFromSortQueryParam() throws Exception {
        mockMvc.perform(get("/test/representation")
                        .param("sort", "nome desc"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("sort_empty=false")));
    }

    @Test
    void shouldReturnEmptyBooleanBuilderWhenNoFilterProvided() throws Exception {
        mockMvc.perform(get("/test/representation"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("filter=false")));
    }

    @Test
    void shouldReturn400ForUnknownFilterField() throws Exception {
        mockMvc.perform(get("/test/representation")
                        .param("filter", "inexistente = 1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400ForInvalidBooleanLiteral() throws Exception {
        mockMvc.perform(get("/test/representation")
                        .param("filter", "ativo = banana"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400ForExtraTokensInSort() throws Exception {
        mockMvc.perform(get("/test/representation")
                        .param("sort", "nome asc lixo"))
                .andExpect(status().isBadRequest());
    }

    @RestController
    @RequestMapping("/test/representation")
    static class SampleRepresentationController {

        @GetMapping
        @com.br.pokefichas.commons.representation.annotation.Representation(SampleProvider.class)
        public ResponseEntity<String> findAll(
                @RepresentationFilter final BooleanBuilder filter,
                @RepresentationSort final Sort sort) {
            final boolean sortEmpty = sort == null || sort.isEmpty();
            return ResponseEntity.ok("filter=" + filter.hasValue() + ",sort_empty=" + sortEmpty);
        }
    }

    public static class SampleProvider implements RepresentationProvider<Object, Object> {

        static final NumberPath<Long> idPath = Expressions.numberPath(Long.class, "id");
        static final StringPath nomePath = Expressions.stringPath("nome");
        static final com.querydsl.core.types.dsl.BooleanPath ativoPath =
                Expressions.booleanPath("ativo");

        @Override
        public Representation<Object, Object> getRepresentation() {
            return RepresentationBuilder.create(Object.class, Object.class)
                    .filterableAndSortable("id", idPath)
                    .filterableAndSortable("nome", nomePath)
                    .filterable("ativo", ativoPath)
                    .defaultSort(Sort.of(idPath.desc()))
                    .build();
        }
    }
}
