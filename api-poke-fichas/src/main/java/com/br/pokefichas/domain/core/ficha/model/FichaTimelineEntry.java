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
        name = "ficha_timeline_entradas",
        indexes = {
                @Index(name = "idx_ficha_timeline_entradas_ficha", columnList = "id_ficha, ordem")
        }
)
public class FichaTimelineEntry extends OrgBaseEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "id_ficha", nullable = false)
    private Long idFicha;

    @Column(name = "secao", nullable = false, length = 80)
    private String secao;

    @Column(name = "periodo", length = 120)
    private String periodo;

    @Column(name = "titulo", nullable = false, length = 180)
    private String titulo;

    @Column(name = "subtitulo", length = 255)
    private String subtitulo;

    @Column(name = "conteudo", nullable = false, columnDefinition = "TEXT")
    private String conteudo;

    @Column(name = "cor", length = 24)
    private String cor;

    @Column(name = "ordem", nullable = false)
    private Integer ordem = 0;

    protected FichaTimelineEntry() {
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

    public String getSecao() {
        return secao;
    }

    public String getPeriodo() {
        return periodo;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getSubtitulo() {
        return subtitulo;
    }

    public String getConteudo() {
        return conteudo;
    }

    public String getCor() {
        return cor;
    }

    public Integer getOrdem() {
        return ordem;
    }

    public static class Builder extends DefaultEntityBuilder<FichaTimelineEntry> {

        private Builder(final FichaTimelineEntry entry, final EntityState state) {
            super(entry, state);
        }

        public static Builder create() {
            return new Builder(new FichaTimelineEntry(), EntityState.NEW);
        }

        public Builder idOrganizacao(final Long idOrganizacao) {
            entity.setIdOrganizacao(idOrganizacao);
            return this;
        }

        public Builder idFicha(final Long idFicha) {
            entity.idFicha = idFicha;
            return this;
        }

        public Builder secao(final String secao) {
            entity.secao = secao;
            return this;
        }

        public Builder periodo(final String periodo) {
            entity.periodo = periodo;
            return this;
        }

        public Builder titulo(final String titulo) {
            entity.titulo = titulo;
            return this;
        }

        public Builder subtitulo(final String subtitulo) {
            entity.subtitulo = subtitulo;
            return this;
        }

        public Builder conteudo(final String conteudo) {
            entity.conteudo = conteudo;
            return this;
        }

        public Builder cor(final String cor) {
            entity.cor = cor;
            return this;
        }

        public Builder ordem(final Integer ordem) {
            entity.ordem = ordem;
            return this;
        }

        @Override
        protected void afterValidate() {
        }
    }
}
