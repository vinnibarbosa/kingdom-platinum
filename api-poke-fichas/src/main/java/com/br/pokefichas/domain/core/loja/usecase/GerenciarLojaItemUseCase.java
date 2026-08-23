package com.br.pokefichas.domain.core.loja.usecase;

import com.br.pokefichas.commons.exception.EntityNotFoundException;
import com.br.pokefichas.commons.organizacao.OrganizacaoContext;
import com.br.pokefichas.domain.core.loja.dto.LojaItemRequest;
import com.br.pokefichas.domain.core.loja.dto.LojaItemResponse;
import com.br.pokefichas.domain.core.loja.dto.ImportarCatalogoLojaRequest;
import com.br.pokefichas.domain.core.loja.dto.ImportarCatalogoLojaResponse;
import com.br.pokefichas.domain.core.loja.model.LojaItem;
import com.br.pokefichas.domain.core.loja.model.LojaMapper;
import com.br.pokefichas.domain.core.loja.repository.LojaItemCommand;
import com.br.pokefichas.domain.core.loja.repository.LojaItemQuery;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Component
public class GerenciarLojaItemUseCase {
    private final LojaItemCommand command;
    private final LojaItemQuery query;
    private final LojaMapper mapper;
    private final OrganizacaoContext organizacaoContext;

    public GerenciarLojaItemUseCase(final LojaItemCommand command, final LojaItemQuery query,
                                    final LojaMapper mapper, final OrganizacaoContext organizacaoContext) {
        this.command = command; this.query = query; this.mapper = mapper; this.organizacaoContext = organizacaoContext;
    }

    @Transactional
    public LojaItemResponse criar(final LojaItemRequest request) {
        return mapper.toResponse(command.save(mapper.toEntity(request, organizacaoContext.getRequiredOrganizacaoId())));
    }

    @Transactional
    public LojaItemResponse atualizar(final Long id, final LojaItemRequest request) {
        final LojaItem item = query.findById(id).orElseThrow(() -> new EntityNotFoundException("Item da loja nao encontrado."));
        return mapper.toResponse(command.save(mapper.toEntity(item, request)));
    }

    @Transactional
    public void excluir(final Long id) {
        command.delete(query.findById(id).orElseThrow(() -> new EntityNotFoundException("Item da loja nao encontrado.")));
    }

    @Transactional
    public ImportarCatalogoLojaResponse importarCatalogo(final ImportarCatalogoLojaRequest request) {
        final Long organizacaoId = organizacaoContext.getRequiredOrganizacaoId();
        final Set<String> chavesExistentes = new HashSet<>();
        query.findTodos().stream()
                .forEach(item -> chavesExistentes.add(chave(item.getCodigo(), item.getNome())));

        int importados = 0;
        int ignorados = 0;
        for (final LojaItemRequest item : request.itens()) {
            final String chave = chave(item.codigo(), item.nome());
            if (!chavesExistentes.add(chave)) {
                ignorados++;
                continue;
            }
            command.save(mapper.toEntity(item, organizacaoId));
            importados++;
        }
        return new ImportarCatalogoLojaResponse(importados, ignorados);
    }

    private String chave(final String codigo, final String nome) {
        final String value = codigo == null || codigo.isBlank() ? nome : codigo;
        return value.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "");
    }
}
