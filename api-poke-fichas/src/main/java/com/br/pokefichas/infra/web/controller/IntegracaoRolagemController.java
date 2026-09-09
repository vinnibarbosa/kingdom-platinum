package com.br.pokefichas.infra.web.controller;

import com.br.pokefichas.domain.core.integracao.dto.ConsumirHoneyRequest;
import com.br.pokefichas.domain.core.integracao.dto.ConsumirHoneyResponse;
import com.br.pokefichas.domain.core.integracao.dto.CriarAutorizacaoRolagemResponse;
import com.br.pokefichas.domain.core.integracao.dto.FichaRolagemResponse;
import com.br.pokefichas.domain.core.integracao.dto.SessaoRolagemResponse;
import com.br.pokefichas.domain.core.integracao.dto.TrocarAutorizacaoRolagemRequest;
import com.br.pokefichas.domain.core.integracao.usecase.AutorizarIntegracaoRolagemUseCase;
import com.br.pokefichas.domain.core.integracao.usecase.IntegrarRolagemUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/integracoes/rolagens")
@CrossOrigin(origins = "https://kingdomplatinum.vercel.app")
public class IntegracaoRolagemController {

    private final IntegrarRolagemUseCase integrarRolagem;
    private final AutorizarIntegracaoRolagemUseCase autorizarIntegracao;

    public IntegracaoRolagemController(final IntegrarRolagemUseCase integrarRolagem,
                                       final AutorizarIntegracaoRolagemUseCase autorizarIntegracao) {
        this.integrarRolagem = integrarRolagem;
        this.autorizarIntegracao = autorizarIntegracao;
    }

    @PostMapping("/autorizacoes")
    @Secured({"ROLE_ADMIN", "ROLE_DONO", "ROLE_GERENTE", "ROLE_OPERADOR"})
    public ResponseEntity<CriarAutorizacaoRolagemResponse> criarAutorizacao() {
        return ResponseEntity.ok(autorizarIntegracao.criar());
    }

    @PostMapping("/sessoes")
    public ResponseEntity<SessaoRolagemResponse> trocarAutorizacao(
            @Valid @RequestBody final TrocarAutorizacaoRolagemRequest request) {
        return ResponseEntity.ok(autorizarIntegracao.trocar(request));
    }

    @GetMapping("/fichas")
    @Secured({"ROLE_ROLL_INTEGRATION", "ROLE_ADMIN", "ROLE_DONO", "ROLE_GERENTE", "ROLE_OPERADOR"})
    public ResponseEntity<List<FichaRolagemResponse>> listarFichas() {
        return ResponseEntity.ok(integrarRolagem.listarFichas());
    }

    @PostMapping("/honey")
    @Secured({"ROLE_ROLL_INTEGRATION", "ROLE_ADMIN", "ROLE_DONO", "ROLE_GERENTE", "ROLE_OPERADOR"})
    public ResponseEntity<ConsumirHoneyResponse> consumirHoney(
            @Valid @RequestBody final ConsumirHoneyRequest request) {
        return ResponseEntity.ok(integrarRolagem.consumirHoney(request));
    }
}
