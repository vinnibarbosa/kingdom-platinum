package com.br.pokefichas.commons.useraccess.provider;

import org.springframework.security.core.Authentication;

public interface UserAccessProvider {
    Authentication getAuthentication();
}
