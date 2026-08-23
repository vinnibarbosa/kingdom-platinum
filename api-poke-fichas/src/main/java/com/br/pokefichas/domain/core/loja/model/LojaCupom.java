package com.br.pokefichas.domain.core.loja.model;

import com.br.pokefichas.commons.builder.DefaultEntityBuilder;
import com.br.pokefichas.commons.entity.OrgBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "loja_cupons")
public class LojaCupom extends OrgBaseEntity<Long> {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id") private Long id;
    @Column(name = "codigo", nullable = false, length = 40) private String codigo;
    @Column(name = "percentual", nullable = false) private Integer percentual;
    @Column(name = "ativo", nullable = false) private boolean ativo = true;

    protected LojaCupom() { }
    @Override public Long getId() { return id; }
    @Override public void setId(final Long id) { this.id = id; }
    public String getCodigo() { return codigo; }
    public Integer getPercentual() { return percentual; }
    public boolean isAtivo() { return ativo; }

    public static class Builder extends DefaultEntityBuilder<LojaCupom> {
        private Builder(final LojaCupom coupon, final EntityState state) { super(coupon, state); }
        public static Builder create() { return new Builder(new LojaCupom(), EntityState.NEW); }
        public static Builder from(final LojaCupom coupon) { return new Builder(coupon, EntityState.BUILT); }
        public Builder idOrganizacao(final Long value) { entity.setIdOrganizacao(value); return this; }
        public Builder codigo(final String value) { entity.codigo = value; return this; }
        public Builder percentual(final Integer value) { entity.percentual = value; return this; }
        public Builder ativo(final boolean value) { entity.ativo = value; return this; }
        @Override protected void afterValidate() { }
    }
}
