package com.br.pokefichas.domain.core.usuario;

import com.br.pokefichas.commons.exception.BusinessException;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class UsuarioTenantIntegrationTest {

    @Autowired
    private UsuarioCommand usuarioCommand;

    @Autowired
    private EntidadeCommand entidadeCommand;

    @Autowired
    private OrganizacaoCommand organizacaoCommand;

    @Test
    @Transactional
    void shouldRejectDuplicatedUsernameAcrossDifferentTenants() {
        final Organizacao organizacao = criarOrganizacao("Org Multi-Tenant");
        final Entidade entidadeA = criarEntidade(organizacao.getId(), "Entidade A");
        final Entidade entidadeB = criarEntidade(organizacao.getId(), "Entidade B");

        usuarioCommand.save(criarUsuario(entidadeA.getId(), "usuario.tenant"));

        assertThatThrownBy(() -> usuarioCommand.save(criarUsuario(entidadeB.getId(), "usuario.tenant")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Ja existe um usuario com o username usuario.tenant");
    }

    @Test
    @Transactional
    void shouldRejectDuplicatedUsernameInsideSameTenant() {
        final Organizacao organizacao = criarOrganizacao("Org Single-Tenant");
        final Entidade entidade = criarEntidade(organizacao.getId(), "Entidade A");
        usuarioCommand.save(criarUsuario(entidade.getId(), "usuario.tenant"));

        assertThatThrownBy(() -> criarUsuario(entidade.getId(), "usuario.tenant"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Ja existe um usuario com o username usuario.tenant");
    }

    private Organizacao criarOrganizacao(final String nome) {
        return organizacaoCommand.save(Organizacao.Builder.create()
                .nome(nome)
                .ativo(true)
                .build());
    }

    private Entidade criarEntidade(final Long idOrganizacao, final String nome) {
        return entidadeCommand.save(Entidade.Builder.create()
                .idOrganizacao(idOrganizacao)
                .nome(nome)
                .ativo(true)
                .build());
    }

    private Usuario criarUsuario(final Long idEntidade, final String username) {
        return Usuario.Builder.create()
                .idEntidade(idEntidade)
                .username(username)
                .nome("Usuario Tenant")
                .senha("senha-segura")
                .perfil(Perfil.ADMIN)
                .ativo(true)
                .build();
    }
}
