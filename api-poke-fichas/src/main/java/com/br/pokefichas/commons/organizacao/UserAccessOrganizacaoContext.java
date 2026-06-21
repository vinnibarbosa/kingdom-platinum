package com.br.pokefichas.commons.organizacao;

import com.br.pokefichas.commons.useraccess.UserAccess;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserAccessOrganizacaoContext implements OrganizacaoContext {

    private final UserAccess userAccess;

    public UserAccessOrganizacaoContext(final UserAccess userAccess) {
        this.userAccess = userAccess;
    }

    @Override
    public Optional<Long> getCurrentOrganizacaoId() {
        return userAccess.getIdOrganizacao();
    }
}
