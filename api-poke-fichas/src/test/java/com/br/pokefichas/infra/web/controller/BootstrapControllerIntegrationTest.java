package com.br.pokefichas.infra.web.controller;

import com.br.pokefichas.commons.security.dto.AuthRequest;
import com.br.pokefichas.domain.core.bootstrap.dto.BootstrapRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class BootstrapControllerIntegrationTest {

    @org.springframework.beans.factory.annotation.Autowired
    private WebApplicationContext context;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    @Transactional
    void shouldBootstrapSystemAndAllowFirstLogin() throws Exception {
        final BootstrapRequest request = new BootstrapRequest("owner.bootstrap", "senha123");

        mockMvc.perform(post("/bootstrap")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idOrganizacao", notNullValue()))
                .andExpect(jsonPath("$.idEntidade", notNullValue()))
                .andExpect(jsonPath("$.idUsuario", notNullValue()))
                .andExpect(jsonPath("$.username", is("owner.bootstrap")));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AuthRequest("owner.bootstrap", "senha123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andExpect(header().string("Set-Cookie", containsString("refresh_token=")))
                .andExpect(header().string("Set-Cookie", not(containsString("access_token="))))
                .andExpect(jsonPath("$.usuario.username", is("owner.bootstrap")));
    }

    @Test
    @Transactional
    void shouldRejectSecondBootstrapExecution() throws Exception {
        final BootstrapRequest request = new BootstrapRequest("owner.bootstrap", "senha123");

        mockMvc.perform(post("/bootstrap")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/bootstrap")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message", is("Bootstrap inicial do sistema ja foi executado")));
    }
}
