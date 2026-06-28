package com.br.pokefichas.infra.web.controller;

import com.br.pokefichas.commons.dto.PageRequest;
import com.br.pokefichas.commons.page.Page;
import com.br.pokefichas.domain.core.ficha.dto.AtualizarFichaRequest;
import com.br.pokefichas.domain.core.ficha.dto.CriarFichaRequest;
import com.br.pokefichas.domain.core.ficha.dto.FichaHistoricoResponse;
import com.br.pokefichas.domain.core.ficha.dto.FichaResponse;
import com.br.pokefichas.domain.core.ficha.dto.FichaResumoResponse;
import com.br.pokefichas.domain.core.ficha.usecase.AtualizarFichaUseCase;
import com.br.pokefichas.domain.core.ficha.usecase.BuscarFichaUseCase;
import com.br.pokefichas.domain.core.ficha.usecase.CriarFichaUseCase;
import com.br.pokefichas.domain.core.ficha.usecase.ExcluirFichaUseCase;
import com.br.pokefichas.domain.core.ficha.usecase.ListarFichasUseCase;
import com.br.pokefichas.domain.core.ficha.usecase.ListarHistoricoFichaUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/fichas")
@Tag(name = "Fichas", description = "APIs para gerenciamento de fichas de treinador")
public class FichaController {

    private final CriarFichaUseCase criar;
    private final BuscarFichaUseCase buscar;
    private final ListarFichasUseCase listar;
    private final AtualizarFichaUseCase atualizar;
    private final ListarHistoricoFichaUseCase listarHistorico;
    private final ExcluirFichaUseCase excluir;

    public FichaController(final CriarFichaUseCase criar,
                           final BuscarFichaUseCase buscar,
                           final ListarFichasUseCase listar,
                           final AtualizarFichaUseCase atualizar,
                           final ListarHistoricoFichaUseCase listarHistorico,
                           final ExcluirFichaUseCase excluir) {
        this.criar = criar;
        this.buscar = buscar;
        this.listar = listar;
        this.atualizar = atualizar;
        this.listarHistorico = listarHistorico;
        this.excluir = excluir;
    }

    @PostMapping
    @Secured({"ROLE_ADMIN", "ROLE_DONO", "ROLE_GERENTE", "ROLE_OPERADOR"})
    @Operation(summary = "Criar ficha")
    public ResponseEntity<FichaResponse> create(@Valid @RequestBody final CriarFichaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(criar.handle(request));
    }

    @GetMapping("/{id}")
    @Secured({"ROLE_ADMIN", "ROLE_DONO", "ROLE_GERENTE", "ROLE_OPERADOR"})
    @Operation(summary = "Buscar ficha por ID")
    public ResponseEntity<FichaResponse> findById(@PathVariable final Long id) {
        return ResponseEntity.ok(buscar.handle(id));
    }

    @GetMapping("/{id}/administracao")
    @Secured("ROLE_ADMIN")
    @Operation(summary = "Buscar qualquer ficha para administracao")
    public ResponseEntity<FichaResponse> findByIdForAdmin(@PathVariable final Long id) {
        return ResponseEntity.ok(buscar.handleAdmin(id));
    }

    @GetMapping("/publicas/{id}")
    @Operation(summary = "Visualizar ficha publica por ID")
    public ResponseEntity<FichaResponse> findPublicById(@PathVariable final Long id) {
        return ResponseEntity.ok(buscar.handlePublico(id));
    }

    @GetMapping("/publicas/slug/{slug}")
    @Operation(summary = "Visualizar ficha publica por nome")
    public ResponseEntity<FichaResponse> findPublicBySlug(@PathVariable final String slug) {
        return ResponseEntity.ok(buscar.handlePublico(slug));
    }

    @GetMapping
    @Secured({"ROLE_ADMIN", "ROLE_DONO", "ROLE_GERENTE", "ROLE_OPERADOR"})
    @Operation(summary = "Listar fichas")
    public ResponseEntity<Page<FichaResumoResponse>> findAll(final PageRequest pageRequest) {
        return ResponseEntity.ok(listar.handle(pageRequest));
    }

    @GetMapping("/{id}/historico")
    @Secured("ROLE_ADMIN")
    @Operation(summary = "Listar historico administrativo da ficha")
    public ResponseEntity<List<FichaHistoricoResponse>> findHistory(@PathVariable final Long id) {
        return ResponseEntity.ok(listarHistorico.handle(id));
    }

    @DeleteMapping("/{id}")
    @Secured("ROLE_ADMIN")
    @Operation(summary = "Excluir ficha")
    public ResponseEntity<Void> delete(@PathVariable final Long id) {
        excluir.handle(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    @Secured({"ROLE_ADMIN", "ROLE_DONO", "ROLE_GERENTE", "ROLE_OPERADOR"})
    @Operation(summary = "Atualizar ficha")
    public ResponseEntity<FichaResponse> update(
            @PathVariable final Long id,
            @Valid @RequestBody final AtualizarFichaRequest request) {
        return ResponseEntity.ok(atualizar.handle(id, request));
    }

    @PutMapping("/{id}/administracao")
    @Secured("ROLE_ADMIN")
    @Operation(summary = "Atualizar qualquer ficha como administrador")
    public ResponseEntity<FichaResponse> updateForAdmin(
            @PathVariable final Long id,
            @Valid @RequestBody final AtualizarFichaRequest request) {
        return ResponseEntity.ok(atualizar.handleAdmin(id, request));
    }
}
