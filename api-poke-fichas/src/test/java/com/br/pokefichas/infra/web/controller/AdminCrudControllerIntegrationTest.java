package com.br.pokefichas.infra.web.controller;

import com.br.pokefichas.domain.core.entidade.dto.CriarEntidadeRequest;
import com.br.pokefichas.domain.core.entidade.model.Entidade;
import com.br.pokefichas.domain.core.entidade.repository.EntidadeCommand;
import com.br.pokefichas.domain.core.organizacao.dto.CriarOrganizacaoRequest;
import com.br.pokefichas.domain.core.organizacao.model.Organizacao;
import com.br.pokefichas.domain.core.organizacao.repository.OrganizacaoCommand;
import com.br.pokefichas.domain.core.usuario.model.Usuario;
import com.br.pokefichas.domain.core.usuario.model.enums.Perfil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class AdminCrudControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private OrganizacaoCommand organizacaoCommand;

    @Autowired
    private EntidadeCommand entidadeCommand;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @Transactional
    void shouldCreateAndListOrganizacoesThroughController() throws Exception {
        final Organizacao organizacaoExistente = organizacaoCommand.save(Organizacao.Builder.create()
                .nome("Org Existente")
                .ativo(true)
                .build());

        mockMvc.perform(post("/organizacoes")
                        .with(csrf())
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authentication(1L, organizacaoExistente.getId(), Perfil.DONO)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CriarOrganizacaoRequest("Org Nova", true))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.nome", is("Org Nova")));

        mockMvc.perform(get("/organizacoes")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authentication(1L, organizacaoExistente.getId(), Perfil.GERENTE))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    @Transactional
    void shouldCreateEntidadeUsingAuthenticatedOrganizacaoContext() throws Exception {
        final Organizacao organizacaoAutenticada = organizacaoCommand.save(Organizacao.Builder.create()
                .nome("Org Autenticada")
                .ativo(true)
                .build());
        final Organizacao outraOrganizacao = organizacaoCommand.save(Organizacao.Builder.create()
                .nome("Org Alternativa")
                .ativo(true)
                .build());

        final CriarEntidadeRequest request = new CriarEntidadeRequest(
                outraOrganizacao.getId(),
                "Entidade Nova",
                "Fantasia Nova",
                null,
                null,
                true
        );

        mockMvc.perform(post("/entidades")
                        .with(csrf())
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authentication(1L, organizacaoAutenticada.getId(), Perfil.ADMIN)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome", is("Entidade Nova")));
    }

    @Test
    @Transactional
    void shouldListOnlyEntidadesFromAuthenticatedOrganizacao() throws Exception {
        final Organizacao organizacaoA = organizacaoCommand.save(Organizacao.Builder.create()
                .nome("Org A")
                .ativo(true)
                .build());
        final Organizacao organizacaoB = organizacaoCommand.save(Organizacao.Builder.create()
                .nome("Org B")
                .ativo(true)
                .build());

        entidadeCommand.save(Entidade.Builder.create()
                .idOrganizacao(organizacaoA.getId())
                .nome("Entidade A")
                .ativo(true)
                .build());
        entidadeCommand.save(Entidade.Builder.create()
                .idOrganizacao(organizacaoB.getId())
                .nome("Entidade B")
                .ativo(true)
                .build());

        mockMvc.perform(get("/entidades")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authentication(1L, organizacaoA.getId(), Perfil.GERENTE))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].nome", is("Entidade A")));
    }

    private UsernamePasswordAuthenticationToken authentication(final Long idEntidade,
                                                               final Long idOrganizacao,
                                                               final Perfil perfil) {
        final Usuario principal = Usuario.Builder.create()
                .username("admin." + idOrganizacao)
                .idEntidade(idEntidade)
                .nome("Admin Teste")
                .senha("senha")
                .perfil(perfil)
                .ativo(true)
                .build(false);
        principal.setIdOrganizacao(idOrganizacao);

        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }
}
