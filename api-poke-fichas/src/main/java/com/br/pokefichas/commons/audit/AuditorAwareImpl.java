package com.br.pokefichas.commons.audit;

import com.br.pokefichas.commons.useraccess.UserAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

public class AuditorAwareImpl implements AuditorAware<String> {

    private static final Logger log = LoggerFactory.getLogger(AuditorAwareImpl.class);

    private final UserAccess userAccess;

    public AuditorAwareImpl(final UserAccess userAccess) {
        this.userAccess = userAccess;
    }

    @Override
    public Optional<String> getCurrentAuditor() {
        try {
            return userAccess.getUsername();
        } catch (final RuntimeException e) {
            log.warn("Error getting current auditor: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
