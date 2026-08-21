package com.br.pokefichas.domain.core.loja.repository;

import com.br.pokefichas.commons.persistence.JpaRepository;
import com.br.pokefichas.commons.page.Sort;
import com.br.pokefichas.domain.core.loja.model.LojaItem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static com.br.pokefichas.domain.core.loja.model.QLojaItem.lojaItem;

@Component
public class LojaItemQuery {
    private final JpaRepository repository;

    public LojaItemQuery(final JpaRepository repository) { this.repository = repository; }

    public List<LojaItem> findAtivos() {
        return repository.findAll(LojaItem.class, Sort.of(lojaItem.ordem.asc(), lojaItem.nome.asc()), lojaItem.ativo.isTrue());
    }

    public List<LojaItem> findTodos() {
        return repository.findAll(LojaItem.class, Sort.of(lojaItem.ordem.asc(), lojaItem.nome.asc()));
    }

    public Optional<LojaItem> findById(final Long id) { return repository.findOptional(LojaItem.class, id); }
}
