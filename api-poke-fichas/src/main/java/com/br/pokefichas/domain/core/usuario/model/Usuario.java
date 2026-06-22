package com.br.pokefichas.domain.core.usuario.model;

import com.br.pokefichas.commons.builder.DefaultEntityBuilder;
import com.br.pokefichas.commons.context.DomainServices;
import com.br.pokefichas.commons.entity.TenantBaseEntity;
import com.br.pokefichas.domain.core.usuario.model.enums.Perfil;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Entity
@Table(
        name = "usuarios",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_usuarios_username", columnNames = {"username"})
        },
        indexes = {
                @Index(name = "idx_usuarios_id_entidade", columnList = "id_entidade"),
                @Index(name = "idx_usuarios_nome", columnList = "nome")
        }
)
public class Usuario extends TenantBaseEntity<Long> implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "senha", nullable = false, length = 255)
    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(name = "perfil", nullable = false, length = 1)
    private Perfil perfil;

    @Column(name = "ativo")
    private boolean ativo = true;

    @Column(name = "auth_version", nullable = false)
    private int authVersion;

    @Column(name = "senha_redefinicao_pendente", nullable = false)
    private boolean senhaRedefinicaoPendente;

    @Transient
    private Long idOrganizacao;

    protected Usuario() {
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public static String normalizeUsername(final String username) {
        return username == null ? null : username.trim().toLowerCase(Locale.ROOT);
    }

    public String getNome() {
        return nome;
    }

    public String getSenha() {
        return senha;
    }

    public Perfil getPerfil() {
        return perfil;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public int getAuthVersion() {
        return authVersion;
    }

    public boolean isSenhaRedefinicaoPendente() {
        return senhaRedefinicaoPendente;
    }

    public Long getIdOrganizacao() {
        return idOrganizacao;
    }

    public void setIdOrganizacao(final Long idOrganizacao) {
        this.idOrganizacao = idOrganizacao;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + perfil.name()));
    }

    @Override
    public String getPassword() {
        return senha;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return ativo;
    }

    @Override
    protected void beforeCreate() {
        final UsuarioDomainService domainService = DomainServices.get(UsuarioDomainService.class);
        domainService.validarTenantObrigatorio(this);
        domainService.validarPerfilObrigatorio(this);
        domainService.validarUnicidadeUsernameCriacao(this);
    }

    @Override
    protected void beforeUpdate() {
        final UsuarioDomainService domainService = DomainServices.get(UsuarioDomainService.class);
        domainService.validarTenantObrigatorio(this);
        domainService.validarPerfilObrigatorio(this);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final Usuario that = (Usuario) obj;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public static class Builder extends DefaultEntityBuilder<Usuario> {

        private Builder(final Usuario usuario, final EntityState state) {
            super(usuario, state);
        }

        public static Builder create() {
            return new Builder(new Usuario(), EntityState.NEW);
        }

        public static Builder from(final Usuario usuario) {
            return new Builder(usuario, EntityState.BUILT);
        }

        @Override
        protected void afterValidate() {
        }

        public Builder username(final String username) {
            entity.username = normalizeUsername(username);
            return this;
        }

        public Builder nome(final String nome) {
            entity.nome = nome;
            return this;
        }

        public Builder idEntidade(final Long idEntidade) {
            entity.setIdEntidade(idEntidade);
            return this;
        }

        public Builder senha(final String senha) {
            entity.senha = senha;
            return this;
        }

        public Builder senhaRedefinicaoPendente(final boolean senhaRedefinicaoPendente) {
            entity.senhaRedefinicaoPendente = senhaRedefinicaoPendente;
            return this;
        }

        public Builder perfil(final Perfil perfil) {
            entity.perfil = perfil;
            return this;
        }

        public Builder ativo(final boolean ativo) {
            entity.ativo = ativo;
            return this;
        }
    }
}
