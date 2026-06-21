package com.br.pokefichas.commons.persistence;

import com.br.pokefichas.domain.core.ficha.model.Ficha;
import com.br.pokefichas.domain.core.organizacao.model.Organizacao;
import com.br.pokefichas.domain.core.organizacao.repository.OrganizacaoCommand;
import com.br.pokefichas.domain.core.usuario.model.Usuario;
import com.br.pokefichas.domain.core.usuario.model.enums.Perfil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class TenantJpaRepositoryIntegrationTest {

    @Autowired
    private JpaRepository repository;

    @Autowired
    private OrganizacaoCommand organizacaoCommand;

    @Autowired
    private JdbcClient jdbcClient;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @Transactional
    void shouldApplyOrganizacaoAutomaticallyOnSaveAndQueries() {
        final Organizacao organizacaoA = organizacaoCommand.save(Organizacao.Builder.create()
                .nome("Org Ficha A")
                .ativo(true)
                .build());

        final Organizacao organizacaoB = organizacaoCommand.save(Organizacao.Builder.create()
                .nome("Org Ficha B")
                .ativo(true)
                .build());

        authenticateAs(organizacaoA.getId());

        final Ficha ficha = Ficha.Builder.create()
                .nome("Ficha Visivel")
                .build(false);

        repository.save(ficha);

        jdbcClient.sql("""
                insert into fichas (
                    id_organizacao,
                    nome,
                    version,
                    created_at,
                    updated_at
                ) values (
                    :idOrganizacao,
                    :nome,
                    0,
                    current_timestamp,
                    current_timestamp
                )
                """)
                .param("idOrganizacao", organizacaoB.getId())
                .param("nome", "Ficha Invisivel")
                .update();

        final Long outraFichaId = jdbcClient.sql("select id from fichas where nome = :nome")
                .param("nome", "Ficha Invisivel")
                .query(Long.class)
                .single();

        final List<Ficha> fichasVisiveis = repository.findAll(Ficha.class);
        final Optional<Ficha> fichaOutraOrganizacao = repository.findOptional(Ficha.class, outraFichaId);

        assertThat(ficha.getIdOrganizacao()).isEqualTo(organizacaoA.getId());
        assertThat(fichasVisiveis).extracting(Ficha::getId).containsExactly(ficha.getId());
        assertThat(fichaOutraOrganizacao).isEmpty();
    }

    private void authenticateAs(final Long idOrganizacao) {
        final Usuario principal = Usuario.Builder.create()
                .username("organizacao.user")
                .idEntidade(1L)
                .nome("Organizacao User")
                .senha("senha")
                .perfil(Perfil.ADMIN)
                .ativo(true)
                .build(false);

        principal.setIdOrganizacao(idOrganizacao);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );
    }

}
