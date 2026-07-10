package com.br.pokefichas.infra.web.controller;

import com.br.pokefichas.domain.core.pokemon.dto.CustomPokemonResponse;
import com.br.pokefichas.domain.core.pokemon.usecase.SupabasePokemonCatalogUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/pokemon/custom")
@Tag(name = "Pokemon customizados", description = "APIs para consulta de Pokemon customizados")
public class CustomPokemonController {

    private final SupabasePokemonCatalogUseCase catalog;

    public CustomPokemonController(final SupabasePokemonCatalogUseCase catalog) {
        this.catalog = catalog;
    }

    @GetMapping
    @Operation(summary = "Pesquisar Pokemon customizados")
    public ResponseEntity<List<CustomPokemonResponse>> search(@RequestParam(required = false, defaultValue = "") final String termo) {
        return ResponseEntity.ok(catalog.search(termo));
    }

    @GetMapping("/{name}")
    @Operation(summary = "Buscar Pokemon customizado por nome")
    public ResponseEntity<CustomPokemonResponse> findByName(@PathVariable final String name) {
        return catalog.findByName(name)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
