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
        name = "ficha_pokemon_movimentos",
        indexes = {
                @Index(name = "idx_ficha_pokemon_movimentos_id_pokemon", columnList = "id_pokemon")
        }
)
public class FichaPokemonMovimento extends OrgBaseEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "id_ficha", nullable = false)
    private Long idFicha;

    @Column(name = "id_pokemon", nullable = false)
    private Long idPokemon;

    @Column(name = "nome", nullable = false, length = 120)
    private String nome;

    @Column(name = "categoria", length = 40)
    private String categoria;

    @Column(name = "tipo", length = 40)
    private String tipo;

    @Column(name = "style", length = 20)
    private String style;

    @Column(name = "poder")
    private Integer poder;

    @Column(name = "accuracy")
    private Integer accuracy;

    @Column(name = "ordem", nullable = false)
    private Integer ordem = 0;

    protected FichaPokemonMovimento() {
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

    public Long getIdPokemon() {
        return idPokemon;
    }

    public String getNome() {
        return nome;
    }

    public String getCategoria() {
        return categoria;
    }

    public String getTipo() {
        return tipo;
    }

    public String getStyle() {
        return style;
    }

    public Integer getPoder() {
        return poder;
    }

    public Integer getAccuracy() {
        return accuracy;
    }

    public Integer getOrdem() {
        return ordem;
    }

    public static class Builder extends DefaultEntityBuilder<FichaPokemonMovimento> {

        private Builder(final FichaPokemonMovimento movimento, final EntityState state) {
            super(movimento, state);
        }

        public static Builder create() {
            return new Builder(new FichaPokemonMovimento(), EntityState.NEW);
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

        public Builder idPokemon(final Long idPokemon) {
            entity.idPokemon = idPokemon;
            return this;
        }

        public Builder nome(final String nome) {
            entity.nome = nome;
            return this;
        }

        public Builder categoria(final String categoria) {
            entity.categoria = categoria;
            return this;
        }

        public Builder tipo(final String tipo) {
            entity.tipo = tipo;
            return this;
        }

        public Builder style(final String style) {
            entity.style = style;
            return this;
        }

        public Builder poder(final Integer poder) {
            entity.poder = poder;
            return this;
        }

        public Builder accuracy(final Integer accuracy) {
            entity.accuracy = accuracy;
            return this;
        }

        public Builder ordem(final Integer ordem) {
            entity.ordem = ordem;
            return this;
        }
    }
}
