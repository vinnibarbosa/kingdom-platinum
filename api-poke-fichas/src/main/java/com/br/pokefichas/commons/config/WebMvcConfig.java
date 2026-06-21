package com.br.pokefichas.commons.config;

import com.br.pokefichas.commons.representation.web.RepresentationFilterResolver;
import com.br.pokefichas.commons.representation.web.RepresentationSortResolver;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class WebMvcConfig {

    @Bean
    public SmartInitializingSingleton representationResolverPrecedence(
            final RequestMappingHandlerAdapter adapter,
            final RepresentationFilterResolver filterResolver,
            final RepresentationSortResolver sortResolver) {
        return () -> {
            final List<HandlerMethodArgumentResolver> resolvers = new ArrayList<>();
            resolvers.add(filterResolver);
            resolvers.add(sortResolver);
            final List<HandlerMethodArgumentResolver> existing = adapter.getArgumentResolvers();
            if (existing != null) {
                resolvers.addAll(existing);
            }
            adapter.setArgumentResolvers(resolvers);
        };
    }
}
