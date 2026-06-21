package com.br.pokefichas.commons.config;

import com.br.pokefichas.commons.organizacao.OrganizacaoContext;
import com.br.pokefichas.commons.persistence.JpaRepository;
import com.br.pokefichas.commons.tenant.TenantContext;
import com.br.pokefichas.commons.useraccess.UserAccess;
import com.querydsl.core.types.dsl.PathBuilderFactory;
import com.querydsl.jpa.JPQLTemplates;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PersistenceConfiguration {

    @PersistenceContext
    private EntityManager entityManager;

    @Bean
    public JpaRepository jpaRepository(
            final EntityManager entityManager,
            final PathBuilderFactory pathBuilderFactory,
            final JPQLTemplates jpqlTemplates,
            final UserAccess userAccess,
            final TenantContext tenantContext,
            final OrganizacaoContext organizacaoContext) {
        return new JpaRepository(entityManager, pathBuilderFactory, jpqlTemplates,
                userAccess, tenantContext, organizacaoContext);
    }

    @Bean
    public PathBuilderFactory pathBuilderFactory() {
        return new PathBuilderFactory();
    }

    @Bean
    public JPQLTemplates jpqlTemplates() {
        return JPQLTemplates.DEFAULT;
    }

    @Bean
    public JPAQueryFactory jpaQueryFactory(final EntityManager entityManager) {
        return new JPAQueryFactory(jpqlTemplates(), entityManager);
    }
}
