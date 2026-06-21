package com.br.pokefichas.commons.entity;

import com.br.pokefichas.commons.organizacao.OrgScoped;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class OrgBaseEntity<I> extends BaseEntity<I> implements OrgScoped {

    @Column(name = "id_organizacao", nullable = false)
    private Long idOrganizacao;

    @Override
    public Long getIdOrganizacao() {
        return idOrganizacao;
    }

    @Override
    public void setIdOrganizacao(final Long idOrganizacao) {
        this.idOrganizacao = idOrganizacao;
    }
}
