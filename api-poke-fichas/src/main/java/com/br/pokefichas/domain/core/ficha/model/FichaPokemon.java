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
        name = "ficha_pokemons",
        indexes = {
                @Index(name = "idx_ficha_pokemons_id_ficha", columnList = "id_ficha"),
                @Index(name = "idx_ficha_pokemons_especie", columnList = "especie")
        }
)
public class FichaPokemon extends OrgBaseEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "id_ficha", nullable = false)
    private Long idFicha;

    @Column(name = "box", length = 80)
    private String box;

    @Column(name = "pokebola", length = 80)
    private String pokebola;

    @Column(name = "apelido", nullable = false, length = 120)
    private String apelido;

    @Column(name = "especie", nullable = false, length = 120)
    private String especie;

    @Column(name = "sprite", columnDefinition = "TEXT")
    private String sprite;

    @Column(name = "genero", length = 20)
    private String genero;

    @Column(name = "sobre", columnDefinition = "TEXT")
    private String sobre;

    @Column(name = "ability", length = 120)
    private String ability;

    @Column(name = "feature", length = 120)
    private String feature;

    @Column(name = "mecanica", length = 80)
    private String mecanica;

    @Column(name = "nature", length = 120)
    private String nature;

    @Column(name = "hold_item", length = 120)
    private String holdItem;

    @Column(name = "hold_item_icon", columnDefinition = "TEXT")
    private String holdItemIcon;

    @Column(name = "happiness_atual")
    private Integer happinessAtual;

    @Column(name = "happiness_max")
    private Integer happinessMax;

    @Column(name = "combo", columnDefinition = "TEXT")
    private String combo;

    @Column(name = "mini_upgrade")
    private Integer miniUpgrade;

    @Column(name = "slot_upgrade")
    private Integer slotUpgrade;

    @Column(name = "hp")
    private Integer hp;

    @Column(name = "atk")
    private Integer atk;

    @Column(name = "def")
    private Integer def;

    @Column(name = "satk")
    private Integer satk;

    @Column(name = "sdef")
    private Integer sdef;

    @Column(name = "speed")
    private Integer speed;

    @Column(name = "pwr")
    private Integer pwr;

    @Column(name = "stm")
    private Integer stm;

    @Column(name = "skl")
    private Integer skl;

    @Column(name = "jmp")
    private Integer jmp;

    @Column(name = "contest_speed")
    private Integer contestSpeed;

    @Column(name = "ordem", nullable = false)
    private Integer ordem = 0;

    protected FichaPokemon() {
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

    public String getBox() {
        return box;
    }

    public String getPokebola() {
        return pokebola;
    }

    public String getApelido() {
        return apelido;
    }

    public String getEspecie() {
        return especie;
    }

    public String getSprite() {
        return sprite;
    }

    public String getGenero() {
        return genero;
    }

    public String getSobre() {
        return sobre;
    }

    public String getAbility() {
        return ability;
    }

    public String getFeature() {
        return feature;
    }

    public String getMecanica() {
        return mecanica;
    }

    public String getNature() {
        return nature;
    }

    public String getHoldItem() {
        return holdItem;
    }

    public String getHoldItemIcon() {
        return holdItemIcon;
    }

    public Integer getHappinessAtual() {
        return happinessAtual;
    }

    public Integer getHappinessMax() {
        return happinessMax;
    }

    public String getCombo() {
        return combo;
    }

    public Integer getMiniUpgrade() {
        return miniUpgrade;
    }

    public Integer getSlotUpgrade() {
        return slotUpgrade;
    }

    public Integer getHp() {
        return hp;
    }

    public Integer getAtk() {
        return atk;
    }

    public Integer getDef() {
        return def;
    }

    public Integer getSatk() {
        return satk;
    }

    public Integer getSdef() {
        return sdef;
    }

    public Integer getSpeed() {
        return speed;
    }

    public Integer getPwr() {
        return pwr;
    }

    public Integer getStm() {
        return stm;
    }

    public Integer getSkl() {
        return skl;
    }

    public Integer getJmp() {
        return jmp;
    }

    public Integer getContestSpeed() {
        return contestSpeed;
    }

    public Integer getOrdem() {
        return ordem;
    }

    public static class Builder extends DefaultEntityBuilder<FichaPokemon> {

        private Builder(final FichaPokemon pokemon, final EntityState state) {
            super(pokemon, state);
        }

        public static Builder create() {
            return new Builder(new FichaPokemon(), EntityState.NEW);
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

        public Builder box(final String box) {
            entity.box = box;
            return this;
        }

        public Builder pokebola(final String pokebola) {
            entity.pokebola = pokebola;
            return this;
        }

        public Builder apelido(final String apelido) {
            entity.apelido = apelido;
            return this;
        }

        public Builder especie(final String especie) {
            entity.especie = especie;
            return this;
        }

        public Builder sprite(final String sprite) {
            entity.sprite = sprite;
            return this;
        }

        public Builder genero(final String genero) {
            entity.genero = genero;
            return this;
        }

        public Builder sobre(final String sobre) {
            entity.sobre = sobre;
            return this;
        }

        public Builder ability(final String ability) {
            entity.ability = ability;
            return this;
        }

        public Builder feature(final String feature) {
            entity.feature = feature;
            return this;
        }

        public Builder mecanica(final String mecanica) {
            entity.mecanica = mecanica;
            return this;
        }

        public Builder nature(final String nature) {
            entity.nature = nature;
            return this;
        }

        public Builder holdItem(final String holdItem) {
            entity.holdItem = holdItem;
            return this;
        }

        public Builder holdItemIcon(final String holdItemIcon) {
            entity.holdItemIcon = holdItemIcon;
            return this;
        }

        public Builder happinessAtual(final Integer happinessAtual) {
            entity.happinessAtual = happinessAtual;
            return this;
        }

        public Builder happinessMax(final Integer happinessMax) {
            entity.happinessMax = happinessMax;
            return this;
        }

        public Builder combo(final String combo) {
            entity.combo = combo;
            return this;
        }

        public Builder miniUpgrade(final Integer miniUpgrade) {
            entity.miniUpgrade = miniUpgrade;
            return this;
        }

        public Builder slotUpgrade(final Integer slotUpgrade) {
            entity.slotUpgrade = slotUpgrade;
            return this;
        }

        public Builder hp(final Integer hp) {
            entity.hp = hp;
            return this;
        }

        public Builder atk(final Integer atk) {
            entity.atk = atk;
            return this;
        }

        public Builder def(final Integer def) {
            entity.def = def;
            return this;
        }

        public Builder satk(final Integer satk) {
            entity.satk = satk;
            return this;
        }

        public Builder sdef(final Integer sdef) {
            entity.sdef = sdef;
            return this;
        }

        public Builder speed(final Integer speed) {
            entity.speed = speed;
            return this;
        }

        public Builder pwr(final Integer pwr) {
            entity.pwr = pwr;
            return this;
        }

        public Builder stm(final Integer stm) {
            entity.stm = stm;
            return this;
        }

        public Builder skl(final Integer skl) {
            entity.skl = skl;
            return this;
        }

        public Builder jmp(final Integer jmp) {
            entity.jmp = jmp;
            return this;
        }

        public Builder contestSpeed(final Integer contestSpeed) {
            entity.contestSpeed = contestSpeed;
            return this;
        }

        public Builder ordem(final Integer ordem) {
            entity.ordem = ordem;
            return this;
        }
    }
}
