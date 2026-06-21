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

import java.time.LocalDate;
import java.time.Instant;

@Entity
@Table(
        name = "ficha_registros",
        indexes = {
                @Index(name = "idx_ficha_registros_id_ficha", columnList = "id_ficha"),
                @Index(name = "idx_ficha_registros_data", columnList = "data_registro")
        }
)
public class FichaRegistro extends OrgBaseEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "id_ficha", nullable = false)
    private Long idFicha;

    @Column(name = "tipo_movimento", nullable = false, length = 20)
    private String tipoMovimento;

    @Column(name = "descricao", nullable = false, columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "data_registro")
    private LocalDate dataRegistro;

    @Column(name = "registrado_em", nullable = false)
    private Instant registradoEm;

    @Column(name = "registrado_por", length = 255)
    private String registradoPor;

    @Column(name = "ordem", nullable = false)
    private Integer ordem = 0;

    protected FichaRegistro() {
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

    public String getTipoMovimento() {
        return tipoMovimento;
    }

    public String getDescricao() {
        return descricao;
    }

    public LocalDate getDataRegistro() {
        return dataRegistro;
    }

    public Instant getRegistradoEm() {
        return registradoEm;
    }

    public String getRegistradoPor() {
        return registradoPor;
    }

    public Integer getOrdem() {
        return ordem;
    }

    public static class Builder extends DefaultEntityBuilder<FichaRegistro> {

        private Builder(final FichaRegistro registro, final EntityState state) {
            super(registro, state);
        }

        public static Builder create() {
            return new Builder(new FichaRegistro(), EntityState.NEW);
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

        public Builder tipoMovimento(final String tipoMovimento) {
            entity.tipoMovimento = tipoMovimento;
            return this;
        }

        public Builder descricao(final String descricao) {
            entity.descricao = descricao;
            return this;
        }

        public Builder dataRegistro(final LocalDate dataRegistro) {
            entity.dataRegistro = dataRegistro;
            return this;
        }

        public Builder registradoEm(final Instant registradoEm) {
            entity.registradoEm = registradoEm;
            return this;
        }

        public Builder registradoPor(final String registradoPor) {
            entity.registradoPor = registradoPor;
            return this;
        }

        public Builder ordem(final Integer ordem) {
            entity.ordem = ordem;
            return this;
        }
    }
}
