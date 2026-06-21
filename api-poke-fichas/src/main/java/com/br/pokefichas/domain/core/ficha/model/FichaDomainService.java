package com.br.pokefichas.domain.core.ficha.model;

import com.br.pokefichas.commons.exception.BusinessException;
import com.br.pokefichas.domain.core.ficha.repository.FichaQuery;
import org.springframework.stereotype.Component;

@Component
public class FichaDomainService {

    private final FichaQuery query;

    public FichaDomainService(final FichaQuery query) {
        this.query = query;
    }

    public void validarNomeObrigatorio(final Ficha ficha) {
        if (ficha.getNome() == null || ficha.getNome().isBlank()) {
            throw new BusinessException("Nome da ficha e obrigatorio");
        }
    }

    public void validarUnicidadeNome(final Ficha ficha) {
        if (query.existsByNome(ficha.getIdOrganizacao(), ficha.getNome(), ficha.getId())) {
            throw new BusinessException("Ja existe uma ficha com este nome para este usuario");
        }
    }
}
