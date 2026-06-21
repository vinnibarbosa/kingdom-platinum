package com.br.pokefichas.domain.core.entidade.repository;

import com.br.pokefichas.commons.dto.PageRequest;
import com.br.pokefichas.commons.page.Page;
import com.br.pokefichas.commons.page.Pageable;
import com.br.pokefichas.commons.page.Sort;
import com.br.pokefichas.commons.persistence.JpaRepository;
import com.br.pokefichas.domain.core.entidade.model.Entidade;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.br.pokefichas.domain.core.entidade.model.QEntidade.entidade;

@Component
public class EntidadeQuery {

    private final JpaRepository repository;

    public EntidadeQuery(final JpaRepository repository) {
        this.repository = repository;
    }

    public Optional<Entidade> findById(final Long id) {
        return repository.findOptional(Entidade.class, id);
    }

    public Optional<Entidade> findByIdWithoutContext(final Long id) {
        return repository.findUniqueOptionalWithoutTenantFilter(Entidade.class, entidade.id.eq(id));
    }

    public Page<Entidade> findAll(final PageRequest pageRequest) {
        final Pageable pageable = pageRequest.toPageable(Sort.of(entidade.nome.asc(), entidade.id.asc()));
        return repository.findAll(Entidade.class, pageable);
    }

}
