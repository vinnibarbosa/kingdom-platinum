package com.br.pokefichas.domain.core.ficha.model;

import com.br.pokefichas.commons.builder.DefaultEntityBuilder;
import com.br.pokefichas.commons.entity.OrgBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(
        name = "ficha_habilidades",
        indexes = {
                @Index(name = "idx_ficha_habilidades_id_ficha", columnList = "id_ficha")
        }
)
public class FichaHabilidade extends OrgBaseEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "id_ficha", nullable = false)
    private Long idFicha;

    @Column(name = "nome", nullable = false, length = 120)
    private String nome;

    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "ordem", nullable = false)
    private Integer ordem = 0;

    protected FichaHabilidade() {
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    public Long getIdFicha() {
        return idFicha;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public Integer getOrdem() {
        return ordem;
    }

    public static class Builder extends DefaultEntityBuilder<FichaHabilidade> {

        private Builder(final FichaHabilidade habilidade, final EntityState state) {
            super(habilidade, state);
        }

        public static Builder create() {
            return new Builder(new FichaHabilidade(), EntityState.NEW);
        }

        @Override
        protected void afterValidate() {
        }

        public Builder idOrganizacao(final Long idOrganizacao) {
            entity.setIdOrganizacao(idOrganizacao);
            return this;
        }

        public Builder idFicha(final Long idFicha) {
            entity.idFicha = idFicha;
            return this;
        }

        public Builder nome(final String nome) {
            entity.nome = nome;
            return this;
        }

        public Builder descricao(final String descricao) {
            entity.descricao = descricao;
            return this;
        }

        public Builder ordem(final Integer ordem) {
            entity.ordem = ordem;
            return this;
        }
    }
}
