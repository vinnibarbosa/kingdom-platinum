package com.br.pokefichas.commons.entity;

import com.br.pokefichas.commons.tenant.TenantScoped;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class TenantBaseEntity<I> extends BaseEntity<I> implements TenantScoped {

    @Column(name = "id_entidade", nullable = false)
    private Long idEntidade;

    @Override
    public Long getIdEntidade() {
        return idEntidade;
    }

    @Override
    public void setIdEntidade(final Long idEntidade) {
        this.idEntidade = idEntidade;
    }
}
