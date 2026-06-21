package com.br.pokefichas.infra.web.controller;

import com.br.pokefichas.commons.dto.PageRequest;
import com.br.pokefichas.commons.page.Page;
import com.br.pokefichas.domain.core.organizacao.dto.AtualizarOrganizacaoRequest;
import com.br.pokefichas.domain.core.organizacao.dto.CriarOrganizacaoRequest;
import com.br.pokefichas.domain.core.organizacao.dto.OrganizacaoResponse;
import com.br.pokefichas.domain.core.organizacao.usecase.AtualizarOrganizacaoUseCase;
import com.br.pokefichas.domain.core.organizacao.usecase.BuscarOrganizacaoUseCase;
import com.br.pokefichas.domain.core.organizacao.usecase.CriarOrganizacaoUseCase;
import com.br.pokefichas.domain.core.organizacao.usecase.ListarOrganizacoesUseCase;
import com.br.pokefichas.infra.security.authorization.organizacao.PodeConsultarOrganizacao;
import com.br.pokefichas.infra.security.authorization.organizacao.PodeGerenciarOrganizacao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/organizacoes")
@Tag(name = "Organizacoes", description = "APIs para gerenciamento de organizacoes")
public class OrganizacaoController {

    private final CriarOrganizacaoUseCase criar;
    private final BuscarOrganizacaoUseCase buscar;
    private final ListarOrganizacoesUseCase listar;
    private final AtualizarOrganizacaoUseCase atualizar;

    public OrganizacaoController(final CriarOrganizacaoUseCase criar,
                                 final BuscarOrganizacaoUseCase buscar,
                                 final ListarOrganizacoesUseCase listar,
                                 final AtualizarOrganizacaoUseCase atualizar) {
        this.criar = criar;
        this.buscar = buscar;
        this.listar = listar;
        this.atualizar = atualizar;
    }

    @PostMapping
    @PodeGerenciarOrganizacao
    @Operation(summary = "Criar organizacao")
    public ResponseEntity<OrganizacaoResponse> create(@Valid @RequestBody final CriarOrganizacaoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(criar.handle(request));
    }

    @GetMapping("/{id}")
    @PodeConsultarOrganizacao
    @Operation(summary = "Buscar organizacao por ID")
    public ResponseEntity<OrganizacaoResponse> findById(@PathVariable final Long id) {
        return ResponseEntity.ok(buscar.handle(id));
    }

    @GetMapping
    @PodeConsultarOrganizacao
    @Operation(summary = "Listar organizacoes")
    public ResponseEntity<Page<OrganizacaoResponse>> findAll(final PageRequest pageRequest) {
        return ResponseEntity.ok(listar.handle(pageRequest));
    }

    @PutMapping("/{id}")
    @PodeGerenciarOrganizacao
    @Operation(summary = "Atualizar organizacao")
    public ResponseEntity<OrganizacaoResponse> update(
            @PathVariable final Long id,
            @Valid @RequestBody final AtualizarOrganizacaoRequest request) {
        return ResponseEntity.ok(atualizar.handle(id, request));
    }
}
