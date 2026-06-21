package com.br.pokefichas.domain.core.entidade.repository;

import com.br.pokefichas.commons.persistence.JpaRepository;
import com.br.pokefichas.domain.core.entidade.model.Entidade;
import org.springframework.stereotype.Component;

@Component
public class EntidadeCommand {

    private final JpaRepository repository;

    public EntidadeCommand(final JpaRepository repository) {
        this.repository = repository;
    }

    public Entidade save(final Entidade entidade) {
        return repository.save(entidade);
    }
}
