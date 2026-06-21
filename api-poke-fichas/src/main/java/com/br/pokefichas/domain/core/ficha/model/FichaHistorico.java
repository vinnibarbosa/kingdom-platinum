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
        name = "ficha_historicos",
        indexes = {
                @Index(name = "idx_ficha_historicos_id_ficha", columnList = "id_ficha"),
                @Index(name = "idx_ficha_historicos_created_at", columnList = "created_at")
        }
)
public class FichaHistorico extends OrgBaseEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "id_ficha", nullable = false)
    private Long idFicha;

    @Column(name = "lote", nullable = false, length = 36)
    private String lote;

    @Column(name = "acao", nullable = false, length = 20)
    private String acao;

    @Column(name = "campo", nullable = false, length = 255)
    private String campo;

    @Column(name = "valor_anterior", columnDefinition = "TEXT")
    private String valorAnterior;

    @Column(name = "valor_novo", columnDefinition = "TEXT")
    private String valorNovo;

    protected FichaHistorico() {
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

    public String getLote() {
        return lote;
    }

    public String getAcao() {
        return acao;
    }

    public String getCampo() {
        return campo;
    }

    public String getValorAnterior() {
        return valorAnterior;
    }

    public String getValorNovo() {
        return valorNovo;
    }

    public static class Builder extends DefaultEntityBuilder<FichaHistorico> {

        private Builder(final FichaHistorico historico, final EntityState state) {
            super(historico, state);
        }

        public static Builder create() {
            return new Builder(new FichaHistorico(), EntityState.NEW);
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

        public Builder lote(final String lote) {
            entity.lote = lote;
            return this;
        }

        public Builder acao(final String acao) {
            entity.acao = acao;
            return this;
        }

        public Builder campo(final String campo) {
            entity.campo = campo;
            return this;
        }

        public Builder valorAnterior(final String valorAnterior) {
            entity.valorAnterior = valorAnterior;
            return this;
        }

        public Builder valorNovo(final String valorNovo) {
            entity.valorNovo = valorNovo;
            return this;
        }
    }
}
