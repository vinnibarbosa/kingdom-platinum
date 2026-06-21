package com.br.pokefichas.domain.core.entidade.model;

import com.br.pokefichas.commons.builder.DefaultEntityBuilder;
import com.br.pokefichas.commons.context.DomainServices;
import com.br.pokefichas.commons.entity.OrgBaseEntity;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.util.Objects;

@Entity
@Table(
        name = "entidades",
        indexes = {
                @Index(name = "idx_entidades_nome", columnList = "nome")
        }
)
public class Entidade extends OrgBaseEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "nome", nullable = false, length = 150)
    private String nome;

    @Column(name = "nome_fantasia", length = 150)
    private String nomeFantasia;

    @Column(name = "inscricao_estadual", length = 20)
    private String inscricaoEstadual;

    @Column(name = "telefone", length = 20)
    private String telefone;

    @Column(name = "ativo")
    private boolean ativo = true;

    protected Entidade() {
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

    public String getNomeFantasia() {
        return nomeFantasia;
    }

    public String getInscricaoEstadual() {
        return inscricaoEstadual;
    }

    public String getTelefone() {
        return telefone;
    }

    public boolean isAtivo() {
        return ativo;
    }

    @Override
    protected void beforeCreate() {
        final EntidadeDomainService domainService = DomainServices.get(EntidadeDomainService.class);
        domainService.validarOrganizacaoObrigatoria(this);
    }

    @Override
    protected void beforeUpdate() {
        final EntidadeDomainService domainService = DomainServices.get(EntidadeDomainService.class);
        domainService.validarOrganizacaoObrigatoria(this);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final Entidade that = (Entidade) obj;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public static class Builder extends DefaultEntityBuilder<Entidade> {

        private Builder(final Entidade entidade, final EntityState state) {
            super(entidade, state);
        }

        public static Builder create() {
            return new Builder(new Entidade(), EntityState.NEW);
        }

        public static Builder from(final Entidade entidade) {
            return new Builder(entidade, EntityState.BUILT);
        }

        @Override
        protected void afterValidate() {
        }

        public Builder idOrganizacao(final Long idOrganizacao) {
            entity.setIdOrganizacao(idOrganizacao);
            return this;
        }

        public Builder nome(final String nome) {
            entity.nome = nome;
            return this;
        }

        public Builder nomeFantasia(final String nomeFantasia) {
            entity.nomeFantasia = nomeFantasia;
            return this;
        }

        public Builder inscricaoEstadual(final String inscricaoEstadual) {
            entity.inscricaoEstadual = inscricaoEstadual;
            return this;
        }

        public Builder telefone(final String telefone) {
            entity.telefone = telefone;
            return this;
        }

        public Builder ativo(final boolean ativo) {
            entity.ativo = ativo;
            return this;
        }
    }
}
