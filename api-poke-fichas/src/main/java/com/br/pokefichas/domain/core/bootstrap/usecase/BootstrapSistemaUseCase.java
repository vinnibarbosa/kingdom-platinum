package com.br.pokefichas.domain.core.bootstrap.usecase;

import com.br.pokefichas.commons.exception.BusinessException;
import com.br.pokefichas.domain.core.bootstrap.dto.BootstrapRequest;
import com.br.pokefichas.domain.core.bootstrap.dto.BootstrapResponse;
import com.br.pokefichas.domain.core.entidade.model.Entidade;
import com.br.pokefichas.domain.core.entidade.repository.EntidadeCommand;
import com.br.pokefichas.domain.core.organizacao.model.Organizacao;
import com.br.pokefichas.domain.core.organizacao.repository.OrganizacaoCommand;
import com.br.pokefichas.domain.core.usuario.model.Usuario;
import com.br.pokefichas.domain.core.usuario.model.enums.Perfil;
import com.br.pokefichas.domain.core.usuario.repository.UsuarioCommand;
import com.br.pokefichas.domain.core.usuario.repository.UsuarioQuery;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class BootstrapSistemaUseCase {

    private final OrganizacaoCommand organizacaoCommand;
    private final EntidadeCommand entidadeCommand;
    private final UsuarioCommand usuarioCommand;
    private final UsuarioQuery usuarioQuery;
    private final PasswordEncoder passwordEncoder;

    public BootstrapSistemaUseCase(final OrganizacaoCommand organizacaoCommand,
                                   final EntidadeCommand entidadeCommand,
                                   final UsuarioCommand usuarioCommand,
                                   final UsuarioQuery usuarioQuery,
                                   final PasswordEncoder passwordEncoder) {
        this.organizacaoCommand = organizacaoCommand;
        this.entidadeCommand = entidadeCommand;
        this.usuarioCommand = usuarioCommand;
        this.usuarioQuery = usuarioQuery;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public BootstrapResponse handle(final BootstrapRequest request) {
        if (usuarioQuery.existsAnyWithoutTenant()) {
            throw new BusinessException("Bootstrap inicial do sistema ja foi executado");
        }

        final String nome = request.username().trim();

        final Organizacao organizacao = organizacaoCommand.save(Organizacao.Builder.create()
                .nome("Conta de " + nome)
                .ativo(true)
                .build());

        final Entidade entidade = entidadeCommand.save(Entidade.Builder.create()
                .idOrganizacao(organizacao.getId())
                .nome("Conta de " + nome)
                .ativo(true)
                .build());

        final Usuario usuario = usuarioCommand.save(Usuario.Builder.create()
                .idEntidade(entidade.getId())
                .username(request.username())
                .nome(nome)
                .senha(passwordEncoder.encode(request.senha()))
                .perfil(Perfil.DONO)
                .ativo(true)
                .build());

        return new BootstrapResponse(
                organizacao.getId(),
                entidade.getId(),
                usuario.getId(),
                usuario.getUsername(),
                organizacao.getNome(),
                entidade.getNome()
        );
    }
}
