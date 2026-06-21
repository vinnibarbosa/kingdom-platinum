package com.br.pokefichas.commons.representation.web;

import com.br.pokefichas.commons.page.Sort;
import com.br.pokefichas.commons.representation.RepresentationMapper;
import com.br.pokefichas.commons.representation.annotation.Representation;
import com.br.pokefichas.commons.representation.annotation.RepresentationSort;
import com.br.pokefichas.commons.representation.model.RepresentationProvider;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.lang.reflect.Method;

@Component
public class RepresentationSortResolver implements HandlerMethodArgumentResolver {

    private final RepresentationMapper mapper;

    public RepresentationSortResolver(final RepresentationMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public boolean supportsParameter(final MethodParameter parameter) {
        return parameter.hasParameterAnnotation(RepresentationSort.class)
               && Sort.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object resolveArgument(final MethodParameter parameter, final ModelAndViewContainer mavContainer,
                                  final NativeWebRequest webRequest, final WebDataBinderFactory binderFactory) {
        final RepresentationSort sortAnn = parameter.getParameterAnnotation(RepresentationSort.class);
        final Class<? extends RepresentationProvider<?, ?>> providerClass = resolveProvider(parameter, sortAnn);
        final String rawSort = webRequest.getParameter(sortAnn.param());
        return mapper.parseSort(providerClass, rawSort);
    }

    @SuppressWarnings("unchecked")
    private Class<? extends RepresentationProvider<?, ?>> resolveProvider(final MethodParameter parameter,
                                                                         final RepresentationSort sortAnn) {
        if (!sortAnn.provider().equals(RepresentationSort.DefaultProvider.class)) {
            return sortAnn.provider();
        }
        final Method method = parameter.getMethod();
        if (method == null) {
            throw new IllegalStateException("Nao foi possivel determinar o metodo para @RepresentationSort");
        }
        final Representation repAnn = method.getAnnotation(Representation.class);
        if (repAnn == null) {
            throw new IllegalStateException(
                    "@RepresentationSort em '" + method.getName()
                    + "' requer @Representation no metodo ou provider explicito na anotacao");
        }
        return repAnn.value();
    }
}
