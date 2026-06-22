package com.br.pokefichas.infra.web.controller;

import com.br.pokefichas.commons.security.dto.AuthRequest;
import com.br.pokefichas.domain.core.usuario.dto.RegistrarContaRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @org.springframework.beans.factory.annotation.Autowired
    private WebApplicationContext context;

    @org.springframework.beans.factory.annotation.Autowired
    private JdbcClient jdbcClient;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    @Transactional
    void shouldRegisterAccountWithFourDigitPasswordAndAllowLogin() throws Exception {
        final RegistrarContaRequest request = new RegistrarContaRequest("usuario2011", "2011");

        mockMvc.perform(post("/auth/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AuthRequest(" Usuario2011 ", "2011"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.usuario.username", is("usuario2011")))
                .andExpect(jsonPath("$.usuario.perfil", is("OPERADOR")));
    }

    @Test
    @Transactional
    void shouldRejectUsernameAlreadyUsedIgnoringCaseAndSpaces() throws Exception {
        mockMvc.perform(post("/auth/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegistrarContaRequest("usuario.unico", "2011"))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/auth/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegistrarContaRequest(" Usuario.Unico ", "2011"))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message", is("Este nome de usuário já está em uso")));
    }

    @Test
    @Transactional
    void shouldRegisterNextLoginPasswordWhenResetIsPending() throws Exception {
        mockMvc.perform(post("/auth/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegistrarContaRequest("usuario.reset", "senhaAntiga"))))
                .andExpect(status().isCreated());

        jdbcClient.sql("""
                        update usuarios
                           set senha_redefinicao_pendente = true,
                               auth_version = auth_version + 1
                         where username = :username
                        """)
                .param("username", "usuario.reset")
                .update();

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AuthRequest("usuario.reset", "senhaNova"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AuthRequest("usuario.reset", "senhaAntiga"))))
                .andExpect(status().isUnauthorized());
    }
}
