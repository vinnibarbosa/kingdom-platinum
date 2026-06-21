package com.br.pokefichas.commons.representation.web;

import com.br.pokefichas.commons.representation.RepresentationMapper;
import com.br.pokefichas.commons.representation.annotation.Representation;
import com.br.pokefichas.commons.representation.annotation.RepresentationFilter;
import com.br.pokefichas.commons.representation.model.RepresentationProvider;
import com.querydsl.core.BooleanBuilder;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.lang.reflect.Method;

@Component
public class RepresentationFilterResolver implements HandlerMethodArgumentResolver {

    private final RepresentationMapper mapper;

    public RepresentationFilterResolver(final RepresentationMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public boolean supportsParameter(final MethodParameter parameter) {
        return parameter.hasParameterAnnotation(RepresentationFilter.class)
               && BooleanBuilder.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object resolveArgument(final MethodParameter parameter, final ModelAndViewContainer mavContainer,
                                  final NativeWebRequest webRequest, final WebDataBinderFactory binderFactory) {
        final RepresentationFilter filterAnn = parameter.getParameterAnnotation(RepresentationFilter.class);
        final Class<? extends RepresentationProvider<?, ?>> providerClass = resolveProvider(parameter, filterAnn);
        final String rawFilter = webRequest.getParameter(filterAnn.param());
        return mapper.parseFilter(providerClass, rawFilter);
    }

    @SuppressWarnings("unchecked")
    private Class<? extends RepresentationProvider<?, ?>> resolveProvider(final MethodParameter parameter,
                                                                         final RepresentationFilter filterAnn) {
        if (!filterAnn.provider().equals(RepresentationFilter.DefaultProvider.class)) {
            return filterAnn.provider();
        }
        final Method method = parameter.getMethod();
        if (method == null) {
            throw new IllegalStateException("Nao foi possivel determinar o metodo para @RepresentationFilter");
        }
        final Representation repAnn = method.getAnnotation(Representation.class);
        if (repAnn == null) {
            throw new IllegalStateException(
                    "@RepresentationFilter em '" + method.getName()
                    + "' requer @Representation no metodo ou provider explicito na anotacao");
        }
        return repAnn.value();
    }
}
