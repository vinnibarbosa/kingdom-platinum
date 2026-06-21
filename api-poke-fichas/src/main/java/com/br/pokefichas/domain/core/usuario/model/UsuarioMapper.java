package com.br.pokefichas.domain.core.usuario.model;

import com.br.pokefichas.commons.utils.ObjectUtil;
import com.br.pokefichas.domain.core.usuario.dto.AtualizarUsuarioRequest;
import com.br.pokefichas.domain.core.usuario.dto.CriarUsuarioRequest;
import com.br.pokefichas.domain.core.usuario.dto.UsuarioResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {

    private final PasswordEncoder passwordEncoder;

    public UsuarioMapper(final PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario toEntity(final CriarUsuarioRequest request) {
        return toEntity(request, null);
    }

    public Usuario toEntity(final CriarUsuarioRequest request, final Long idEntidade) {
        return Usuario.Builder.create()
                .idEntidade(idEntidade)
                .username(request.username())
                .nome(request.nome())
                .senha(passwordEncoder.encode(request.senha()))
                .perfil(request.perfil())
                .ativo(ObjectUtil.getIfExists(request.ativo(), ativo -> ativo, true))
                .build();
    }

    public Usuario toEntity(final Usuario usuario, final AtualizarUsuarioRequest request) {
        final Usuario.Builder builder = Usuario.Builder.from(usuario)
                .nome(request.nome())
                .perfil(request.perfil())
                .ativo(request.ativo());

        if (request.senha() != null && !request.senha().trim().isEmpty()) {
            builder.senha(passwordEncoder.encode(request.senha()));
        }

        return builder.build();
    }

    public UsuarioResponse toResponse(final Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getIdEntidade(),
                usuario.getUsername(),
                usuario.getNome(),
                usuario.getPerfil(),
                usuario.isAtivo(),
                usuario.getCreatedAt(),
                usuario.getCreatedBy(),
                usuario.getUpdatedAt(),
                usuario.getUpdatedBy()
        );
    }
}
