package com.br.pokefichas.domain.core.organizacao.repository;

import com.br.pokefichas.commons.persistence.JpaRepository;
import com.br.pokefichas.domain.core.organizacao.model.Organizacao;
import org.springframework.stereotype.Component;

@Component
public class OrganizacaoCommand {

    private final JpaRepository repository;

    public OrganizacaoCommand(final JpaRepository repository) {
        this.repository = repository;
    }

    public Organizacao save(final Organizacao organizacao) {
        return repository.save(organizacao);
    }
}
