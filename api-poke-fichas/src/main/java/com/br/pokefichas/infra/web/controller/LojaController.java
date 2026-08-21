package com.br.pokefichas.infra.web.controller;

import com.br.pokefichas.domain.core.loja.dto.CompraLojaRequest;
import com.br.pokefichas.domain.core.loja.dto.CompraLojaResponse;
import com.br.pokefichas.domain.core.loja.dto.FichaCompraResponse;
import com.br.pokefichas.domain.core.loja.dto.LojaItemRequest;
import com.br.pokefichas.domain.core.loja.dto.LojaItemResponse;
import com.br.pokefichas.domain.core.loja.usecase.ComprarItemLojaUseCase;
import com.br.pokefichas.domain.core.loja.usecase.GerenciarLojaItemUseCase;
import com.br.pokefichas.domain.core.loja.usecase.ListarLojaUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/loja")
public class LojaController {
    private final ListarLojaUseCase listar;
    private final GerenciarLojaItemUseCase gerenciar;
    private final ComprarItemLojaUseCase comprar;

    public LojaController(final ListarLojaUseCase listar, final GerenciarLojaItemUseCase gerenciar,
                          final ComprarItemLojaUseCase comprar) {
        this.listar = listar; this.gerenciar = gerenciar; this.comprar = comprar;
    }

    @GetMapping("/itens")
    public ResponseEntity<List<LojaItemResponse>> listarItens() { return ResponseEntity.ok(listar.ativos()); }

    @GetMapping("/itens/administracao")
    @Secured("ROLE_ADMIN")
    public ResponseEntity<List<LojaItemResponse>> listarTodosItens() { return ResponseEntity.ok(listar.todos()); }

    @GetMapping("/fichas")
    public ResponseEntity<List<FichaCompraResponse>> listarFichasDoUsuario() { return ResponseEntity.ok(listar.fichasDoUsuario()); }

    @PostMapping("/itens")
    @Secured("ROLE_ADMIN")
    public ResponseEntity<LojaItemResponse> criarItem(@Valid @RequestBody final LojaItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(gerenciar.criar(request));
    }

    @PutMapping("/itens/{id}")
    @Secured("ROLE_ADMIN")
    public ResponseEntity<LojaItemResponse> atualizarItem(@PathVariable final Long id,
                                                            @Valid @RequestBody final LojaItemRequest request) {
        return ResponseEntity.ok(gerenciar.atualizar(id, request));
    }

    @DeleteMapping("/itens/{id}")
    @Secured("ROLE_ADMIN")
    public ResponseEntity<Void> excluirItem(@PathVariable final Long id) {
        gerenciar.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/compras")
    public ResponseEntity<CompraLojaResponse> comprar(@Valid @RequestBody final CompraLojaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(comprar.handle(request));
    }
}
