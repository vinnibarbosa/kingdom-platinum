package com.br.pokefichas.domain.core.organizacao.repository;

import com.br.pokefichas.commons.dto.PageRequest;
import com.br.pokefichas.commons.page.Page;
import com.br.pokefichas.commons.page.Pageable;
import com.br.pokefichas.commons.page.Sort;
import com.br.pokefichas.commons.persistence.JpaRepository;
import com.br.pokefichas.domain.core.organizacao.model.Organizacao;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.br.pokefichas.domain.core.organizacao.model.QOrganizacao.organizacao;

@Component
public class OrganizacaoQuery {

    private final JpaRepository repository;

    public OrganizacaoQuery(final JpaRepository repository) {
        this.repository = repository;
    }

    public Optional<Organizacao> findById(final Long id) {
        return repository.findOptional(Organizacao.class, id);
    }

    public Page<Organizacao> findAll(final PageRequest pageRequest) {
        final Pageable pageable = pageRequest.toPageable(Sort.of(organizacao.nome.asc(), organizacao.id.asc()));
        return repository.findAll(Organizacao.class, pageable);
    }
}
