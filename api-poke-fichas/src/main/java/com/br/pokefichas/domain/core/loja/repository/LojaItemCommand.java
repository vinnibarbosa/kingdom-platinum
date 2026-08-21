package com.br.pokefichas.domain.core.loja.repository;

import com.br.pokefichas.commons.persistence.JpaRepository;
import com.br.pokefichas.domain.core.loja.model.LojaItem;
import org.springframework.stereotype.Component;

@Component
public class LojaItemCommand {
    private final JpaRepository repository;
    public LojaItemCommand(final JpaRepository repository) { this.repository = repository; }
    public LojaItem save(final LojaItem item) { return repository.save(item); }
    public void delete(final LojaItem item) { repository.remove(item); }
}
