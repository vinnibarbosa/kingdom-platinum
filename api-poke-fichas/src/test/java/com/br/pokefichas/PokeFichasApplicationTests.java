package com.br.pokefichas;

import com.br.pokefichas.domain.core.entidade.model.Entidade;
import com.br.pokefichas.domain.core.entidade.repository.EntidadeCommand;
import com.br.pokefichas.domain.core.organizacao.model.Organizacao;
import com.br.pokefichas.domain.core.organizacao.repository.OrganizacaoCommand;
import com.br.pokefichas.domain.core.usuario.model.Usuario;
import com.br.pokefichas.domain.core.usuario.model.enums.Perfil;
import com.br.pokefichas.domain.core.usuario.repository.UsuarioCommand;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class PokeFichasApplicationTests {

    @Autowired
    private UsuarioCommand usuarioCommand;

    @Autowired
    private EntidadeCommand entidadeCommand;

    @Autowired
    private OrganizacaoCommand organizacaoCommand;

    @Autowired
    private JdbcClient jdbcClient;

    @Test
    void contextLoads() {
    }

    @Test
    @Transactional
    void shouldPersistPerfilUsingShortCode() {
        final Organizacao organizacao = organizacaoCommand.save(Organizacao.Builder.create()
                .nome("Org Usuario Test")
                .ativo(true)
                .build());

        final Entidade entidade = entidadeCommand.save(Entidade.Builder.create()
                .idOrganizacao(organizacao.getId())
                .nome("Entidade Usuario Test")
                .ativo(true)
                .build());

        final Usuario usuario = Usuario.Builder.create()
                .username("enum.perfil.test")
                .idEntidade(entidade.getId())
                .nome("Enum Perfil Test")
                .senha("senha-segura")
                .perfil(Perfil.ADMIN)
                .ativo(true)
                .build();

        usuarioCommand.save(usuario);

        final String perfilPersistido = jdbcClient.sql("select perfil from usuarios where id = :id")
                .param("id", usuario.getId())
                .query(String.class)
                .single();

        assertThat(perfilPersistido).isEqualTo("A");
    }
}
