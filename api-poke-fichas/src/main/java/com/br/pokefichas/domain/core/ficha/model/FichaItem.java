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
        name = "ficha_itens",
        indexes = {
                @Index(name = "idx_ficha_itens_id_ficha", columnList = "id_ficha"),
                @Index(name = "idx_ficha_itens_categoria", columnList = "categoria")
        }
)
public class FichaItem extends OrgBaseEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "id_ficha", nullable = false)
    private Long idFicha;

    @Column(name = "categoria", nullable = false, length = 60)
    private String categoria;

    @Column(name = "codigo", length = 40)
    private String codigo;

    @Column(name = "icone", columnDefinition = "TEXT")
    private String icone;

    @Column(name = "nome", nullable = false, length = 150)
    private String nome;

    @Column(name = "quantidade", nullable = false)
    private Integer quantidade = 0;

    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "ordem", nullable = false)
    private Integer ordem = 0;

    protected FichaItem() {
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

    public String getCategoria() {
        return categoria;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getIcone() {
        return icone;
    }

    public String getNome() {
        return nome;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public String getDescricao() {
        return descricao;
    }

    public Integer getOrdem() {
        return ordem;
    }

    public static class Builder extends DefaultEntityBuilder<FichaItem> {

        private Builder(final FichaItem item, final EntityState state) {
            super(item, state);
        }

        public static Builder create() {
            return new Builder(new FichaItem(), EntityState.NEW);
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

        public Builder categoria(final String categoria) {
            entity.categoria = categoria;
            return this;
        }

        public Builder codigo(final String codigo) {
            entity.codigo = codigo;
            return this;
        }

        public Builder icone(final String icone) {
            entity.icone = icone;
            return this;
        }

        public Builder nome(final String nome) {
            entity.nome = nome;
            return this;
        }

        public Builder quantidade(final Integer quantidade) {
            entity.quantidade = quantidade;
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
