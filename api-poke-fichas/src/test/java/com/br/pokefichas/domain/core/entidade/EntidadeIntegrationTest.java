package com.br.pokefichas.domain.core.entidade;

import com.br.pokefichas.domain.core.entidade.model.Entidade;
import com.br.pokefichas.domain.core.entidade.repository.EntidadeCommand;
import com.br.pokefichas.domain.core.entidade.repository.EntidadeQuery;
import com.br.pokefichas.domain.core.organizacao.model.Organizacao;
import com.br.pokefichas.domain.core.organizacao.repository.OrganizacaoCommand;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class EntidadeIntegrationTest {

    @Autowired
    private EntidadeCommand entidadeCommand;

    @Autowired
    private EntidadeQuery entidadeQuery;

    @Autowired
    private OrganizacaoCommand organizacaoCommand;

    @Test
    @Transactional
    void shouldPersistEntidadeWithBaseFields() {
        final Organizacao organizacao = criarOrganizacao("Org Entidade Matriz");

        final Entidade entidade = Entidade.Builder.create()
                .idOrganizacao(organizacao.getId())
                .nome("Entidade Matriz")
                .nomeFantasia("Entidade")
                .inscricaoEstadual("123456789")
                .telefone("11999999999")
                .ativo(true)
                .build();

        final Entidade savedEntidade = entidadeCommand.save(entidade);

        final Entidade persistedEntidade = entidadeQuery.findByIdWithoutContext(savedEntidade.getId()).orElseThrow();

        assertThat(persistedEntidade.getId()).isNotNull();
        assertThat(persistedEntidade.getNome()).isEqualTo("Entidade Matriz");
        assertThat(persistedEntidade.getNomeFantasia()).isEqualTo("Entidade");
        assertThat(persistedEntidade.isAtivo()).isTrue();
    }

    private Organizacao criarOrganizacao(final String nome) {
        return organizacaoCommand.save(Organizacao.Builder.create()
                .nome(nome)
                .ativo(true)
                .build());
    }
}
