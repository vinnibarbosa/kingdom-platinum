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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
        final Map<String, LojaItem> itensExistentes = new HashMap<>();
        final List<LojaItem> todosExistentes = query.findTodos();
        todosExistentes.forEach(item -> itensExistentes.putIfAbsent(chave(item.getCodigo(), item.getNome()), item));

        int importados = 0;
        int atualizados = 0;
        int ignorados = 0;
        final Set<Long> itensEncontrados = new HashSet<>();
        for (final LojaItemRequest item : request.itens()) {
            final String chave = chave(item.codigo(), item.nome());
            if (chave.isBlank()) {
                ignorados++;
                continue;
            }
            final LojaItem existente = itensExistentes.get(chave);
            if (existente != null) {
                command.save(sincronizarDadosCatalogo(existente, item));
                itensEncontrados.add(existente.getId());
                atualizados++;
                continue;
            }
            final LojaItem novo = command.save(LojaItem.Builder.from(mapper.toEntity(item, organizacaoId))
                    .gerenciadoCatalogo(true)
                    .build());
            itensExistentes.put(chave, novo);
            itensEncontrados.add(novo.getId());
            importados++;
        }

        int removidos = 0;
        for (final LojaItem existente : todosExistentes) {
            if (existente.isGerenciadoCatalogo() && !itensEncontrados.contains(existente.getId())) {
                command.delete(existente);
                removidos++;
            }
        }
        return new ImportarCatalogoLojaResponse(importados, atualizados, removidos, ignorados);
    }

    private LojaItem sincronizarDadosCatalogo(final LojaItem existente, final LojaItemRequest catalogo) {
        final boolean atualizarIcone = !vazio(catalogo.icone())
                && (vazio(existente.getIcone()) || iconeAutomaticoInvalido(existente.getIcone())
                || !catalogo.icone().equals(existente.getIcone()));
        final boolean atualizarDescricao = !vazio(catalogo.descricao());
        final boolean atualizarCodigo = !vazio(catalogo.codigo());
        final LojaItem.Builder builder = LojaItem.Builder.from(existente)
                .nome(catalogo.nome().trim())
                .categoria(catalogo.categoria().trim())
                .ordem(catalogo.ordem() == null ? 0 : catalogo.ordem())
                .gerenciadoCatalogo(true);
        if (atualizarIcone) builder.icone(catalogo.icone());
        if (atualizarDescricao) builder.descricao(catalogo.descricao());
        if (atualizarCodigo) builder.codigo(catalogo.codigo());
        return builder.build();
    }

    private boolean vazio(final String value) {
        return value == null || value.isBlank();
    }

    private boolean iconeAutomaticoInvalido(final String value) {
        return value != null && value.startsWith("https://raw.githubusercontent.com/PokeAPI/sprites/");
    }

    private String chave(final String codigo, final String nome) {
        final String value = codigo == null || codigo.isBlank() ? nome : codigo;
        return value.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "");
    }
}
