package com.br.pokefichas.infra.web.controller;

import com.br.pokefichas.commons.dto.PageRequest;
import com.br.pokefichas.commons.page.Page;
import com.br.pokefichas.domain.core.entidade.dto.AtualizarEntidadeRequest;
import com.br.pokefichas.domain.core.entidade.dto.CriarEntidadeRequest;
import com.br.pokefichas.domain.core.entidade.dto.EntidadeResponse;
import com.br.pokefichas.domain.core.entidade.usecase.AtualizarEntidadeUseCase;
import com.br.pokefichas.domain.core.entidade.usecase.BuscarEntidadeUseCase;
import com.br.pokefichas.domain.core.entidade.usecase.CriarEntidadeUseCase;
import com.br.pokefichas.domain.core.entidade.usecase.ListarEntidadesUseCase;
import com.br.pokefichas.infra.security.authorization.entidade.PodeConsultarEntidade;
import com.br.pokefichas.infra.security.authorization.entidade.PodeGerenciarEntidade;
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
@RequestMapping("/entidades")
@Tag(name = "Entidades", description = "APIs para gerenciamento de entidades")
public class EntidadeController {

    private final CriarEntidadeUseCase criar;
    private final BuscarEntidadeUseCase buscar;
    private final ListarEntidadesUseCase listar;
    private final AtualizarEntidadeUseCase atualizar;

    public EntidadeController(final CriarEntidadeUseCase criar,
                              final BuscarEntidadeUseCase buscar,
                              final ListarEntidadesUseCase listar,
                              final AtualizarEntidadeUseCase atualizar) {
        this.criar = criar;
        this.buscar = buscar;
        this.listar = listar;
        this.atualizar = atualizar;
    }

    @PostMapping
    @PodeGerenciarEntidade
    @Operation(summary = "Criar entidade")
    public ResponseEntity<EntidadeResponse> create(@Valid @RequestBody final CriarEntidadeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(criar.handle(request));
    }

    @GetMapping("/{id}")
    @PodeConsultarEntidade
    @Operation(summary = "Buscar entidade por ID")
    public ResponseEntity<EntidadeResponse> findById(@PathVariable final Long id) {
        return ResponseEntity.ok(buscar.handle(id));
    }

    @GetMapping
    @PodeConsultarEntidade
    @Operation(summary = "Listar entidades")
    public ResponseEntity<Page<EntidadeResponse>> findAll(final PageRequest pageRequest) {
        return ResponseEntity.ok(listar.handle(pageRequest));
    }

    @PutMapping("/{id}")
    @PodeGerenciarEntidade
    @Operation(summary = "Atualizar entidade")
    public ResponseEntity<EntidadeResponse> update(
            @PathVariable final Long id,
            @Valid @RequestBody final AtualizarEntidadeRequest request) {
        return ResponseEntity.ok(atualizar.handle(id, request));
    }
}
