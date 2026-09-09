package com.br.pokefichas.domain.core.integracao.model;

import com.br.pokefichas.commons.builder.DefaultEntityBuilder;
import com.br.pokefichas.commons.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

@Entity
@Table(
        name = "integracao_autorizacoes",
        uniqueConstraints = @UniqueConstraint(name = "uk_integracao_autorizacoes_codigo", columnNames = "codigo_hash"),
        indexes = @Index(name = "idx_integracao_autorizacoes_expira_em", columnList = "expira_em")
)
public class IntegracaoAutorizacao extends BaseEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_usuario", nullable = false)
    private Long idUsuario;

    @Column(name = "id_entidade", nullable = false)
    private Long idEntidade;

    @Column(name = "id_organizacao", nullable = false)
    private Long idOrganizacao;

    @Column(name = "codigo_hash", nullable = false, length = 64)
    private String codigoHash;

    @Column(name = "expira_em", nullable = false)
    private Instant expiraEm;

    @Column(name = "usado_em")
    private Instant usadoEm;

    protected IntegracaoAutorizacao() {
    }

    @Override
    public Long getId() { return id; }

    @Override
    public void setId(final Long id) { this.id = id; }

    public Long getIdUsuario() { return idUsuario; }
    public Long getIdEntidade() { return idEntidade; }
    public Long getIdOrganizacao() { return idOrganizacao; }
    public String getCodigoHash() { return codigoHash; }
    public Instant getExpiraEm() { return expiraEm; }
    public Instant getUsadoEm() { return usadoEm; }

    public static class Builder extends DefaultEntityBuilder<IntegracaoAutorizacao> {
        private Builder(final IntegracaoAutorizacao entity, final EntityState state) { super(entity, state); }

        public static Builder create() { return new Builder(new IntegracaoAutorizacao(), EntityState.NEW); }
        public static Builder from(final IntegracaoAutorizacao entity) { return new Builder(entity, EntityState.BUILT); }

        public Builder idUsuario(final Long value) { entity.idUsuario = value; return this; }
        public Builder idEntidade(final Long value) { entity.idEntidade = value; return this; }
        public Builder idOrganizacao(final Long value) { entity.idOrganizacao = value; return this; }
        public Builder codigoHash(final String value) { entity.codigoHash = value; return this; }
        public Builder expiraEm(final Instant value) { entity.expiraEm = value; return this; }
        public Builder usadoEm(final Instant value) { entity.usadoEm = value; return this; }
    }
}
