package com.br.pokefichas.domain.core.organizacao.model;

import com.br.pokefichas.commons.builder.DefaultEntityBuilder;
import com.br.pokefichas.commons.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.util.Objects;

@Entity
@Table(
        name = "organizacoes",
        indexes = {
                @Index(name = "idx_organizacoes_nome", columnList = "nome")
        }
)
public class Organizacao extends BaseEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "nome", nullable = false, length = 150)
    private String nome;

    @Column(name = "ativo")
    private boolean ativo = true;

    protected Organizacao() {
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public boolean isAtivo() {
        return ativo;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final Organizacao that = (Organizacao) obj;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public static class Builder extends DefaultEntityBuilder<Organizacao> {

        private Builder(final Organizacao organizacao, final EntityState state) {
            super(organizacao, state);
        }

        public static Builder create() {
            return new Builder(new Organizacao(), EntityState.NEW);
        }

        public static Builder from(final Organizacao organizacao) {
            return new Builder(organizacao, EntityState.BUILT);
        }

        public Builder nome(final String nome) {
            entity.nome = nome;
            return this;
        }

        public Builder ativo(final boolean ativo) {
            entity.ativo = ativo;
            return this;
        }
    }
}
