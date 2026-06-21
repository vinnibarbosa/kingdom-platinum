package com.br.pokefichas.domain.core.entidade.model;

import com.br.pokefichas.commons.exception.BusinessException;
import com.br.pokefichas.domain.core.entidade.repository.EntidadeQuery;
import org.springframework.stereotype.Component;

@Component
public class EntidadeDomainService {

    private final EntidadeQuery query;

    public EntidadeDomainService(final EntidadeQuery query) {
        this.query = query;
    }

    public void validarOrganizacaoObrigatoria(final Entidade entidade) {
        if (entidade.getIdOrganizacao() == null) {
            throw new BusinessException("Organizacao e obrigatoria para a entidade");
        }
    }

}
