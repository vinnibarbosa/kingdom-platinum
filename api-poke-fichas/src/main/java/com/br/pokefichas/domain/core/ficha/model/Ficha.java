package com.br.pokefichas.domain.core.ficha.model;

import com.br.pokefichas.commons.builder.DefaultEntityBuilder;
import com.br.pokefichas.commons.context.DomainServices;
import com.br.pokefichas.commons.entity.OrgBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(
        name = "fichas",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_fichas_organizacao_nome", columnNames = {"id_organizacao", "nome"})
        },
        indexes = {
                @Index(name = "idx_fichas_id_organizacao", columnList = "id_organizacao"),
                @Index(name = "idx_fichas_nome", columnList = "nome")
        }
)
public class Ficha extends OrgBaseEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "nome", nullable = false, length = 150)
    private String nome;

    @Column(name = "frase", length = 255)
    private String frase;

    @Column(name = "idade")
    private Integer idade;

    @Column(name = "naturalidade", length = 120)
    private String naturalidade;

    @Column(name = "classe_personagem", length = 80)
    private String classePersonagem;

    @Column(name = "altura_cm", precision = 6, scale = 2)
    private BigDecimal alturaCm;

    @Column(name = "peso_kg", precision = 6, scale = 2)
    private BigDecimal pesoKg;

    @Column(name = "tipo_fisico", length = 80)
    private String tipoFisico;

    @Column(name = "indole", length = 80)
    private String indole;

    @Column(name = "ranking")
    private Integer ranking;

    @Column(name = "ocupacao", length = 120)
    private String ocupacao;

    @Column(name = "reputacao")
    private Integer reputacao;

    @Column(name = "dinheiro", precision = 15, scale = 2)
    private BigDecimal dinheiro;

    @Column(name = "pontos_vida")
    private Integer pontosVida;

    @Column(name = "equipe", length = 120)
    private String equipe;

    @Column(name = "pontos")
    private Integer pontos;

    @Column(name = "mini_upgrade")
    private Integer miniUpgrade;

    @Column(name = "slot_upgrade")
    private Integer slotUpgrade;

    @Column(name = "cor_tema", length = 24)
    private String corTema;

    @Column(name = "photoplayer", columnDefinition = "TEXT")
    private String photoplayer;

    @Column(name = "avatar", length = 120)
    private String avatar;

    @Column(name = "player", length = 120)
    private String player;

    @Column(name = "biografia", columnDefinition = "TEXT")
    private String biografia;

    @Column(name = "anotacoes", columnDefinition = "TEXT")
    private String anotacoes;

    protected Ficha() {
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public String getFrase() {
        return frase;
    }

    public Integer getIdade() {
        return idade;
    }

    public String getNaturalidade() {
        return naturalidade;
    }

    public String getClassePersonagem() {
        return classePersonagem;
    }

    public BigDecimal getAlturaCm() {
        return alturaCm;
    }

    public BigDecimal getPesoKg() {
        return pesoKg;
    }

    public String getTipoFisico() {
        return tipoFisico;
    }

    public String getIndole() {
        return indole;
    }

    public Integer getRanking() {
        return ranking;
    }

    public String getOcupacao() {
        return ocupacao;
    }

    public Integer getReputacao() {
        return reputacao;
    }

    public BigDecimal getDinheiro() {
        return dinheiro;
    }

    public Integer getPontosVida() {
        return pontosVida;
    }

    public String getEquipe() {
        return equipe;
    }

    public Integer getPontos() {
        return pontos;
    }

    public Integer getMiniUpgrade() {
        return miniUpgrade;
    }

    public Integer getSlotUpgrade() {
        return slotUpgrade;
    }

    public String getCorTema() {
        return corTema;
    }

    public String getPhotoplayer() {
        return photoplayer;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getPlayer() {
        return player;
    }

    public String getBiografia() {
        return biografia;
    }

    public String getAnotacoes() {
        return anotacoes;
    }

    @Override
    protected void beforeCreate() {
        final FichaDomainService domainService = DomainServices.get(FichaDomainService.class);
        domainService.validarNomeObrigatorio(this);
        domainService.validarUnicidadeNome(this);
    }

    @Override
    protected void beforeUpdate() {
        final FichaDomainService domainService = DomainServices.get(FichaDomainService.class);
        domainService.validarNomeObrigatorio(this);
        domainService.validarUnicidadeNome(this);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final Ficha that = (Ficha) obj;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public static class Builder extends DefaultEntityBuilder<Ficha> {

        private Builder(final Ficha ficha, final EntityState state) {
            super(ficha, state);
        }

        public static Builder create() {
            return new Builder(new Ficha(), EntityState.NEW);
        }

        public static Builder from(final Ficha ficha) {
            return new Builder(ficha, EntityState.BUILT);
        }

        @Override
        protected void afterValidate() {
        }

        public Builder idOrganizacao(final Long idOrganizacao) {
            entity.setIdOrganizacao(idOrganizacao);
            return this;
        }

        public Builder nome(final String nome) {
            entity.nome = nome;
            return this;
        }

        public Builder frase(final String frase) {
            entity.frase = frase;
            return this;
        }

        public Builder idade(final Integer idade) {
            entity.idade = idade;
            return this;
        }

        public Builder naturalidade(final String naturalidade) {
            entity.naturalidade = naturalidade;
            return this;
        }

        public Builder classePersonagem(final String classePersonagem) {
            entity.classePersonagem = classePersonagem;
            return this;
        }

        public Builder alturaCm(final BigDecimal alturaCm) {
            entity.alturaCm = alturaCm;
            return this;
        }

        public Builder pesoKg(final BigDecimal pesoKg) {
            entity.pesoKg = pesoKg;
            return this;
        }

        public Builder tipoFisico(final String tipoFisico) {
            entity.tipoFisico = tipoFisico;
            return this;
        }

        public Builder indole(final String indole) {
            entity.indole = indole;
            return this;
        }

        public Builder ranking(final Integer ranking) {
            entity.ranking = ranking;
            return this;
        }

        public Builder ocupacao(final String ocupacao) {
            entity.ocupacao = ocupacao;
            return this;
        }

        public Builder reputacao(final Integer reputacao) {
            entity.reputacao = reputacao;
            return this;
        }

        public Builder dinheiro(final BigDecimal dinheiro) {
            entity.dinheiro = dinheiro;
            return this;
        }

        public Builder pontosVida(final Integer pontosVida) {
            entity.pontosVida = pontosVida;
            return this;
        }

        public Builder equipe(final String equipe) {
            entity.equipe = equipe;
            return this;
        }

        public Builder pontos(final Integer pontos) {
            entity.pontos = pontos;
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

        public Builder corTema(final String corTema) {
            entity.corTema = corTema;
            return this;
        }

        public Builder photoplayer(final String photoplayer) {
            entity.photoplayer = photoplayer;
            return this;
        }

        public Builder avatar(final String avatar) {
            entity.avatar = avatar;
            return this;
        }

        public Builder player(final String player) {
            entity.player = player;
            return this;
        }

        public Builder biografia(final String biografia) {
            entity.biografia = biografia;
            return this;
        }

        public Builder anotacoes(final String anotacoes) {
            entity.anotacoes = anotacoes;
            return this;
        }
    }
}
