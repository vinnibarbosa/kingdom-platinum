package com.br.pokefichas.commons.security.model;

import com.br.pokefichas.commons.builder.DefaultEntityBuilder;
import com.br.pokefichas.commons.entity.BaseEntity;
import com.br.pokefichas.domain.core.usuario.model.Usuario;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "refresh_tokens", indexes = {
        @Index(name = "idx_refresh_tokens_usuario", columnList = "id_usuario"),
        @Index(name = "idx_refresh_tokens_expires_at", columnList = "expires_at")
})
public class RefreshToken extends BaseEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "token", unique = true, nullable = false, length = 255)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "is_revoked")
    private Boolean isRevoked = Boolean.FALSE;

    protected RefreshToken() {
    }

    @Override
    public Long getId() { return id; }

    @Override
    public void setId(final Long id) { this.id = id; }

    public String getToken() { return token; }
    public Usuario getUsuario() { return usuario; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public Boolean getRevoked() { return isRevoked; }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !isExpired() && (isRevoked == null || !isRevoked);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final RefreshToken that = (RefreshToken) obj;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public static class Builder extends DefaultEntityBuilder<RefreshToken> {

        private Builder(final RefreshToken refreshToken, final EntityState state) {
            super(refreshToken, state);
        }

        public static Builder create() {
            return new Builder(new RefreshToken(), EntityState.NEW);
        }

        public static Builder from(final RefreshToken refreshToken) {
            return new Builder(refreshToken, EntityState.BUILT);
        }

        public Builder token(final String token) {
            entity.token = token;
            return this;
        }

        public Builder usuario(final Usuario usuario) {
            entity.usuario = usuario;
            return this;
        }

        public Builder expiresAt(final LocalDateTime expiresAt) {
            entity.expiresAt = expiresAt;
            return this;
        }

        public Builder revoked(final Boolean revoked) {
            entity.isRevoked = revoked;
            return this;
        }

        public Builder expiresInDays(final long days) {
            entity.expiresAt = LocalDateTime.now().plusDays(days);
            return this;
        }
    }
}
