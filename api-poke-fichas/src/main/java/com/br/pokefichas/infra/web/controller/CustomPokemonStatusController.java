package com.br.pokefichas.infra.web.controller;

import com.br.pokefichas.domain.core.pokemon.usecase.SupabasePokemonCatalogUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class CustomPokemonStatusController {

    private final SupabasePokemonCatalogUseCase catalog;

    public CustomPokemonStatusController(final SupabasePokemonCatalogUseCase catalog) {
        this.catalog = catalog;
    }

    @GetMapping({"/actuator/pokemon-custom", "/actuator/pokemon-custom/status"})
    public ResponseEntity<Map<String, Object>> status() {
        return ResponseEntity.ok(catalog.status());
    }
}
