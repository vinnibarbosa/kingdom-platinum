package com.br.pokefichas.domain.core.loja.usecase;

import com.br.pokefichas.commons.exception.BusinessException;
import com.br.pokefichas.commons.exception.EntityNotFoundException;
import com.br.pokefichas.commons.organizacao.OrganizacaoContext;
import com.br.pokefichas.domain.core.loja.dto.LojaCupomRequest;
import com.br.pokefichas.domain.core.loja.dto.LojaCupomResponse;
import com.br.pokefichas.domain.core.loja.model.LojaCupom;
import com.br.pokefichas.domain.core.loja.repository.LojaCupomCommand;
import com.br.pokefichas.domain.core.loja.repository.LojaCupomQuery;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Component
public class GerenciarLojaCupomUseCase {
    private final LojaCupomCommand command;
    private final LojaCupomQuery query;
    private final OrganizacaoContext organizacaoContext;

    public GerenciarLojaCupomUseCase(final LojaCupomCommand command, final LojaCupomQuery query,
                                     final OrganizacaoContext organizacaoContext) {
        this.command = command;
        this.query = query;
        this.organizacaoContext = organizacaoContext;
    }

    @Transactional(readOnly = true)
    public List<LojaCupomResponse> listar() { return query.findTodos().stream().map(this::toResponse).toList(); }

    @Transactional
    public LojaCupomResponse criar(final LojaCupomRequest request) {
        final String codigo = normalizar(request.codigo());
        if (query.findAtivoByCodigo(codigo).isPresent()) {
            throw new BusinessException("Já existe um cupom ativo com este código.", "COUPON_ALREADY_EXISTS");
        }
        return toResponse(command.save(LojaCupom.Builder.create()
                .idOrganizacao(organizacaoContext.getRequiredOrganizacaoId())
                .codigo(codigo)
                .percentual(request.percentual())
                .ativo(request.ativo() == null || request.ativo())
                .build()));
    }

    @Transactional
    public LojaCupomResponse atualizar(final Long id, final LojaCupomRequest request) {
        final LojaCupom current = query.findById(id).orElseThrow(() -> new EntityNotFoundException("Cupom não encontrado."));
        return toResponse(command.save(LojaCupom.Builder.from(current)
                .codigo(normalizar(request.codigo()))
                .percentual(request.percentual())
                .ativo(request.ativo() == null || request.ativo())
                .build()));
    }

    @Transactional
    public void excluir(final Long id) { command.delete(query.findById(id).orElseThrow(() -> new EntityNotFoundException("Cupom não encontrado."))); }

    private LojaCupomResponse toResponse(final LojaCupom cupom) { return new LojaCupomResponse(cupom.getId(), cupom.getCodigo(), cupom.getPercentual(), cupom.isAtivo()); }

    private String normalizar(final String value) { return value.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT); }
}
