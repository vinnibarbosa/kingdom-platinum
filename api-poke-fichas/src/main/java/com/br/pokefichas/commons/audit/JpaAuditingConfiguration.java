package com.br.pokefichas.commons.audit;

import com.br.pokefichas.commons.useraccess.UserAccess;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaAuditingConfiguration {

    @Bean
    public AuditorAware<String> auditorAware(final UserAccess userAccess) {
        return new AuditorAwareImpl(userAccess);
    }
}
