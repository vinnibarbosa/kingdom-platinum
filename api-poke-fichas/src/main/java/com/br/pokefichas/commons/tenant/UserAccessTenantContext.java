package com.br.pokefichas.commons.tenant;

import com.br.pokefichas.commons.useraccess.UserAccess;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserAccessTenantContext implements TenantContext {

    private final UserAccess userAccess;

    public UserAccessTenantContext(final UserAccess userAccess) {
        this.userAccess = userAccess;
    }

    @Override
    public Optional<Long> getCurrentTenantId() {
        return userAccess.getIdEntidade();
    }
}
