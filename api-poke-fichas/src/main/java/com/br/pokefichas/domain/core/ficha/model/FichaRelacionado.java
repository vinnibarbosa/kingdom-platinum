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
        name = "ficha_relacionados",
        indexes = {
                @Index(name = "idx_ficha_relacionados_id_ficha", columnList = "id_ficha")
        }
)
public class FichaRelacionado extends OrgBaseEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "id_ficha", nullable = false)
    private Long idFicha;

    @Column(name = "nome", nullable = false, length = 150)
    private String nome;

    @Column(name = "relacao", length = 120)
    private String relacao;

    @Column(name = "imagem", columnDefinition = "TEXT")
    private String imagem;

    @Column(name = "historia", columnDefinition = "TEXT")
    private String historia;

    @Column(name = "ordem", nullable = false)
    private Integer ordem = 0;

    protected FichaRelacionado() {
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

    public String getRelacao() {
        return relacao;
    }

    public String getImagem() {
        return imagem;
    }

    public String getHistoria() {
        return historia;
    }

    public Integer getOrdem() {
        return ordem;
    }

    public static class Builder extends DefaultEntityBuilder<FichaRelacionado> {

        private Builder(final FichaRelacionado relacionado, final EntityState state) {
            super(relacionado, state);
        }

        public static Builder create() {
            return new Builder(new FichaRelacionado(), EntityState.NEW);
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

        public Builder relacao(final String relacao) {
            entity.relacao = relacao;
            return this;
        }

        public Builder imagem(final String imagem) {
            entity.imagem = imagem;
            return this;
        }

        public Builder historia(final String historia) {
            entity.historia = historia;
            return this;
        }

        public Builder ordem(final Integer ordem) {
            entity.ordem = ordem;
            return this;
        }
    }
}
