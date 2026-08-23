package com.br.pokefichas.domain.core.loja.repository;

import com.br.pokefichas.commons.persistence.JpaRepository;
import com.br.pokefichas.domain.core.loja.model.LojaCupom;
import org.springframework.stereotype.Component;

@Component
public class LojaCupomCommand {
    private final JpaRepository repository;

    public LojaCupomCommand(final JpaRepository repository) { this.repository = repository; }

    public LojaCupom save(final LojaCupom cupom) { return repository.save(cupom); }

    public void delete(final LojaCupom cupom) { repository.remove(cupom); }
}
