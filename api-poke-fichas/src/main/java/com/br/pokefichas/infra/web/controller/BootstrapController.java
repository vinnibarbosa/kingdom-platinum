package com.br.pokefichas.infra.web.controller;

import com.br.pokefichas.domain.core.bootstrap.dto.BootstrapRequest;
import com.br.pokefichas.domain.core.bootstrap.dto.BootstrapResponse;
import com.br.pokefichas.domain.core.bootstrap.usecase.BootstrapSistemaUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bootstrap")
@Profile("!prod")
@Tag(name = "Bootstrap", description = "Endpoint de inicializacao do sistema")
public class BootstrapController {

    private final BootstrapSistemaUseCase bootstrapSistemaUseCase;

    public BootstrapController(final BootstrapSistemaUseCase bootstrapSistemaUseCase) {
        this.bootstrapSistemaUseCase = bootstrapSistemaUseCase;
    }

    @PostMapping
    @Operation(summary = "Executar bootstrap inicial do sistema")
    public ResponseEntity<BootstrapResponse> bootstrap(@Valid @RequestBody final BootstrapRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bootstrapSistemaUseCase.handle(request));
    }
}
