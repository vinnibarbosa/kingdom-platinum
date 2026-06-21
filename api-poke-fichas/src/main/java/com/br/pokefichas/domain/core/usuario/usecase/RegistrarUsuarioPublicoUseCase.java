package com.br.pokefichas.domain.core.usuario.usecase;

import com.br.pokefichas.domain.core.entidade.model.Entidade;
import com.br.pokefichas.domain.core.entidade.repository.EntidadeCommand;
import com.br.pokefichas.domain.core.organizacao.model.Organizacao;
import com.br.pokefichas.domain.core.organizacao.repository.OrganizacaoCommand;
import com.br.pokefichas.domain.core.usuario.dto.RegistrarContaRequest;
import com.br.pokefichas.domain.core.usuario.model.Usuario;
import com.br.pokefichas.domain.core.usuario.model.enums.Perfil;
import com.br.pokefichas.domain.core.usuario.repository.UsuarioCommand;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RegistrarUsuarioPublicoUseCase {

    private final UsuarioCommand usuarioCommand;
    private final PasswordEncoder passwordEncoder;
    private final EntidadeCommand entidadeCommand;
    private final OrganizacaoCommand organizacaoCommand;

    public RegistrarUsuarioPublicoUseCase(final UsuarioCommand usuarioCommand,
                                          final PasswordEncoder passwordEncoder,
                                          final EntidadeCommand entidadeCommand,
                                          final OrganizacaoCommand organizacaoCommand) {
        this.usuarioCommand = usuarioCommand;
        this.passwordEncoder = passwordEncoder;
        this.entidadeCommand = entidadeCommand;
        this.organizacaoCommand = organizacaoCommand;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void handle(final RegistrarContaRequest request) {
        final String nome = request.username().trim();

        final Organizacao organizacao = organizacaoCommand.save(Organizacao.Builder.create()
                .nome("Conta de " + nome)
                .ativo(true)
                .build());

        final Entidade entidade = entidadeCommand.save(Entidade.Builder.create()
                .nome("Conta de " + nome)
                .ativo(true)
                .idOrganizacao(organizacao.getId())
                .build());

        final Usuario usuario = Usuario.Builder.create()
                .username(request.username())
                .nome(nome)
                .senha(passwordEncoder.encode(request.senha()))
                .perfil(Perfil.OPERADOR)
                .ativo(true)
                .idEntidade(entidade.getId())
                .build();

        usuarioCommand.save(usuario);
    }
}
