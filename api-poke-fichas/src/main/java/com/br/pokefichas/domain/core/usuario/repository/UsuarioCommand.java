package com.br.pokefichas.domain.core.usuario.repository;

import com.br.pokefichas.commons.persistence.JpaRepository;
import com.br.pokefichas.domain.core.usuario.model.Usuario;
import org.springframework.stereotype.Component;

@Component
public class UsuarioCommand {

    private final JpaRepository repository;

    public UsuarioCommand(final JpaRepository repository) {
        this.repository = repository;
    }

    public Usuario save(final Usuario usuario) {
        return repository.save(usuario);
    }
}
