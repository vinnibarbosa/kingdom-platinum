package com.br.pokefichas.domain.core.loja.model;

import com.br.pokefichas.commons.builder.DefaultEntityBuilder;
import com.br.pokefichas.commons.entity.OrgBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "loja_itens", indexes = @Index(name = "idx_loja_itens_organizacao_ativo", columnList = "id_organizacao,ativo"))
public class LojaItem extends OrgBaseEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "categoria", nullable = false, length = 60)
    private String categoria;

    @Column(name = "codigo", length = 80)
    private String codigo;

    @Column(name = "icone", columnDefinition = "TEXT")
    private String icone;

    @Column(name = "nome", nullable = false, length = 150)
    private String nome;

    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "preco", nullable = false, precision = 15, scale = 2)
    private BigDecimal preco;

    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;

    @Column(name = "ordem", nullable = false)
    private Integer ordem = 0;

    protected LojaItem() { }

    @Override public Long getId() { return id; }
    @Override public void setId(final Long id) { this.id = id; }
    public String getCategoria() { return categoria; }
    public String getCodigo() { return codigo; }
    public String getIcone() { return icone; }
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public BigDecimal getPreco() { return preco; }
    public boolean isAtivo() { return ativo; }
    public Integer getOrdem() { return ordem; }

    public static class Builder extends DefaultEntityBuilder<LojaItem> {
        private Builder(final LojaItem item, final EntityState state) { super(item, state); }
        public static Builder create() { return new Builder(new LojaItem(), EntityState.NEW); }
        public static Builder from(final LojaItem item) { return new Builder(item, EntityState.BUILT); }
        @Override protected void afterValidate() { }
        public Builder idOrganizacao(final Long value) { entity.setIdOrganizacao(value); return this; }
        public Builder categoria(final String value) { entity.categoria = value; return this; }
        public Builder codigo(final String value) { entity.codigo = value; return this; }
        public Builder icone(final String value) { entity.icone = value; return this; }
        public Builder nome(final String value) { entity.nome = value; return this; }
        public Builder descricao(final String value) { entity.descricao = value; return this; }
        public Builder preco(final BigDecimal value) { entity.preco = value; return this; }
        public Builder ativo(final boolean value) { entity.ativo = value; return this; }
        public Builder ordem(final Integer value) { entity.ordem = value; return this; }
    }
}
