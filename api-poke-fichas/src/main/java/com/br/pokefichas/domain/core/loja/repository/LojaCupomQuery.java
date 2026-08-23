package com.br.pokefichas.domain.core.loja.repository;

import com.br.pokefichas.commons.page.Sort;
import com.br.pokefichas.commons.persistence.JpaRepository;
import com.br.pokefichas.domain.core.loja.model.LojaCupom;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static com.br.pokefichas.domain.core.loja.model.QLojaCupom.lojaCupom;

@Component
public class LojaCupomQuery {
    private final JpaRepository repository;

    public LojaCupomQuery(final JpaRepository repository) { this.repository = repository; }

    public Optional<LojaCupom> findById(final Long id) { return repository.findOptional(LojaCupom.class, id); }

    public Optional<LojaCupom> findAtivoByCodigo(final String codigo) {
        return repository.findUniqueOptional(LojaCupom.class, lojaCupom.codigo.eq(codigo), lojaCupom.ativo.isTrue());
    }

    public List<LojaCupom> findTodos() {
        return repository.findAll(LojaCupom.class, Sort.of(lojaCupom.codigo.asc()));
    }
}
