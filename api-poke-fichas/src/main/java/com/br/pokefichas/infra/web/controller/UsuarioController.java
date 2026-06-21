package com.br.pokefichas.infra.web.controller;

import com.br.pokefichas.commons.dto.PageRequest;
import com.br.pokefichas.commons.page.Page;
import com.br.pokefichas.domain.core.usuario.dto.AtualizarUsuarioRequest;
import com.br.pokefichas.domain.core.usuario.dto.CriarUsuarioRequest;
import com.br.pokefichas.domain.core.usuario.dto.UsuarioResponse;
import com.br.pokefichas.domain.core.usuario.usecase.AtualizarUsuarioUseCase;
import com.br.pokefichas.domain.core.usuario.usecase.BuscarUsuarioUseCase;
import com.br.pokefichas.domain.core.usuario.usecase.CriarUsuarioUseCase;
import com.br.pokefichas.domain.core.usuario.usecase.ListarUsuariosUseCase;
import com.br.pokefichas.infra.security.authorization.usuario.PodeConsultarUsuario;
import com.br.pokefichas.infra.security.authorization.usuario.PodeGerenciarUsuario;
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
@RequestMapping("/usuarios")
@Tag(name = "Usuarios", description = "APIs para gerenciamento de usuarios")
public class UsuarioController {

    private final CriarUsuarioUseCase criar;
    private final BuscarUsuarioUseCase buscar;
    private final ListarUsuariosUseCase listar;
    private final AtualizarUsuarioUseCase atualizar;

    public UsuarioController(final CriarUsuarioUseCase criar,
                             final BuscarUsuarioUseCase buscar,
                             final ListarUsuariosUseCase listar,
                             final AtualizarUsuarioUseCase atualizar) {
        this.criar = criar;
        this.buscar = buscar;
        this.listar = listar;
        this.atualizar = atualizar;
    }

    @PostMapping
    @PodeGerenciarUsuario
    @Operation(summary = "Criar usuario")
    public ResponseEntity<UsuarioResponse> create(@Valid @RequestBody final CriarUsuarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(criar.handle(request));
    }

    @GetMapping("/{id}")
    @PodeConsultarUsuario
    @Operation(summary = "Buscar usuario por ID")
    public ResponseEntity<UsuarioResponse> findById(@PathVariable final Long id) {
        return ResponseEntity.ok(buscar.handle(id));
    }

    @GetMapping
    @PodeGerenciarUsuario
    @Operation(summary = "Listar usuarios")
    public ResponseEntity<Page<UsuarioResponse>> findAll(final PageRequest pageRequest) {
        return ResponseEntity.ok(listar.handle(pageRequest));
    }

    @PutMapping("/{id}")
    @PodeGerenciarUsuario
    @Operation(summary = "Atualizar usuario")
    public ResponseEntity<UsuarioResponse> update(
            @PathVariable final Long id,
            @Valid @RequestBody final AtualizarUsuarioRequest request) {
        return ResponseEntity.ok(atualizar.handle(id, request));
    }
}
