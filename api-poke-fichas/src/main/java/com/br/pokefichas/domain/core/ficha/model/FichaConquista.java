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

@Entity
@Table(
        name = "ficha_conquistas",
        indexes = {
                @Index(name = "idx_ficha_conquistas_id_ficha", columnList = "id_ficha"),
                @Index(name = "idx_ficha_conquistas_tipo", columnList = "tipo")
        }
)
public class FichaConquista extends OrgBaseEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "id_ficha", nullable = false)
    private Long idFicha;

    @Column(name = "tipo", nullable = false, length = 40)
    private String tipo;

    @Column(name = "nome", nullable = false, length = 150)
    private String nome;

    @Column(name = "imagem", columnDefinition = "TEXT")
    private String imagem;

    @Column(name = "data_conquista")
    private LocalDate dataConquista;

    @Column(name = "ordem", nullable = false)
    private Integer ordem = 0;

    protected FichaConquista() {
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

    public String getTipo() {
        return tipo;
    }

    public String getNome() {
        return nome;
    }

    public String getImagem() {
        return imagem;
    }

    public LocalDate getDataConquista() {
        return dataConquista;
    }

    public Integer getOrdem() {
        return ordem;
    }

    public static class Builder extends DefaultEntityBuilder<FichaConquista> {

        private Builder(final FichaConquista conquista, final EntityState state) {
            super(conquista, state);
        }

        public static Builder create() {
            return new Builder(new FichaConquista(), EntityState.NEW);
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

        public Builder tipo(final String tipo) {
            entity.tipo = tipo;
            return this;
        }

        public Builder nome(final String nome) {
            entity.nome = nome;
            return this;
        }

        public Builder imagem(final String imagem) {
            entity.imagem = imagem;
            return this;
        }

        public Builder dataConquista(final LocalDate dataConquista) {
            entity.dataConquista = dataConquista;
            return this;
        }

        public Builder ordem(final Integer ordem) {
            entity.ordem = ordem;
            return this;
        }
    }
}
