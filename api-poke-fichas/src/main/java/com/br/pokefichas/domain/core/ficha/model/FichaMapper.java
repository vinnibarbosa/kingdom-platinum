package com.br.pokefichas.domain.core.ficha.model;

import com.br.pokefichas.commons.utils.ObjectUtil;
import com.br.pokefichas.domain.core.ficha.dto.AtualizarFichaRequest;
import com.br.pokefichas.domain.core.ficha.dto.CriarFichaRequest;
import com.br.pokefichas.domain.core.ficha.dto.FichaConquistaRequest;
import com.br.pokefichas.domain.core.ficha.dto.FichaConquistaResponse;
import com.br.pokefichas.domain.core.ficha.dto.FichaHabilidadeRequest;
import com.br.pokefichas.domain.core.ficha.dto.FichaHabilidadeResponse;
import com.br.pokefichas.domain.core.ficha.dto.FichaItemRequest;
import com.br.pokefichas.domain.core.ficha.dto.FichaItemResponse;
import com.br.pokefichas.domain.core.ficha.dto.FichaPokemonMovimentoRequest;
import com.br.pokefichas.domain.core.ficha.dto.FichaPokemonMovimentoResponse;
import com.br.pokefichas.domain.core.ficha.dto.FichaPokemonRequest;
import com.br.pokefichas.domain.core.ficha.dto.FichaPokemonResponse;
import com.br.pokefichas.domain.core.ficha.dto.FichaPokemonResumoResponse;
import com.br.pokefichas.domain.core.ficha.dto.FichaRegistroRequest;
import com.br.pokefichas.domain.core.ficha.dto.FichaRegistroResponse;
import com.br.pokefichas.domain.core.ficha.dto.FichaRelacionadoRequest;
import com.br.pokefichas.domain.core.ficha.dto.FichaRelacionadoResponse;
import com.br.pokefichas.domain.core.ficha.dto.FichaResponse;
import com.br.pokefichas.domain.core.ficha.dto.FichaResumoResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.time.Instant;

@Component
public class FichaMapper {

    private static final String DEFAULT_THEME_COLOR = "#aeb5bf";
    private static final String LEGACY_GREEN_THEME_COLOR = "#2f6f55";
    private static final String LEGACY_BLUE_THEME_COLOR = "#586a9b";

    public Ficha toEntity(final CriarFichaRequest request, final Long idOrganizacao) {
        return apply(Ficha.Builder.create().idOrganizacao(idOrganizacao), request).build();
    }

    public Ficha toEntity(final Ficha ficha, final AtualizarFichaRequest request) {
        return apply(Ficha.Builder.from(ficha), request).build();
    }

    private Ficha.Builder apply(final Ficha.Builder builder, final CriarFichaRequest request) {
        return builder
                .nome(request.nome())
                .frase(request.frase())
                .idade(request.idade())
                .naturalidade(request.naturalidade())
                .classePersonagem(request.classePersonagem())
                .alturaCm(request.alturaCm())
                .pesoKg(request.pesoKg())
                .tipoFisico(request.tipoFisico())
                .indole(request.indole())
                .ranking(request.ranking())
                .ocupacao(request.ocupacao())
                .reputacao(request.reputacao())
                .dinheiro(request.dinheiro())
                .pontosVida(request.pontosVida())
                .equipe(request.equipe())
                .pontos(request.pontos())
                .miniUpgrade(request.miniUpgrade())
                .slotUpgrade(request.slotUpgrade())
                .corTema(normalizeThemeColor(request.corTema()))
                .photoplayer(request.photoplayer())
                .banner(request.banner())
                .avatar(request.avatar())
                .player(request.player())
                .biografia(request.biografia())
                .anotacoes(request.anotacoes());
    }

    private Ficha.Builder apply(final Ficha.Builder builder, final AtualizarFichaRequest request) {
        return builder
                .nome(request.nome())
                .frase(request.frase())
                .idade(request.idade())
                .naturalidade(request.naturalidade())
                .classePersonagem(request.classePersonagem())
                .alturaCm(request.alturaCm())
                .pesoKg(request.pesoKg())
                .tipoFisico(request.tipoFisico())
                .indole(request.indole())
                .ranking(request.ranking())
                .ocupacao(request.ocupacao())
                .reputacao(request.reputacao())
                .dinheiro(request.dinheiro())
                .pontosVida(request.pontosVida())
                .equipe(request.equipe())
                .pontos(request.pontos())
                .miniUpgrade(request.miniUpgrade())
                .slotUpgrade(request.slotUpgrade())
                .corTema(normalizeThemeColor(request.corTema()))
                .photoplayer(request.photoplayer())
                .banner(request.banner())
                .avatar(request.avatar())
                .player(request.player())
                .biografia(request.biografia())
                .anotacoes(request.anotacoes());
    }

    private String normalizeThemeColor(final String color) {
        if (color == null
                || color.isBlank()
                || LEGACY_GREEN_THEME_COLOR.equalsIgnoreCase(color.trim())
                || LEGACY_BLUE_THEME_COLOR.equalsIgnoreCase(color.trim())) {
            return DEFAULT_THEME_COLOR;
        }
        return color.trim();
    }

    public List<FichaRelacionado> toRelacionados(final List<FichaRelacionadoRequest> requests,
                                                 final Long idFicha,
                                                 final Long idOrganizacao) {
        return Optional.ofNullable(requests).orElse(List.of()).stream()
                .map(request -> FichaRelacionado.Builder.create()
                        .idOrganizacao(idOrganizacao)
                        .idFicha(idFicha)
                        .nome(request.nome())
                        .relacao(request.relacao())
                        .imagem(request.imagem())
                        .historia(request.historia())
                        .ordem(ObjectUtil.getIfExists(request.ordem(), ordem -> ordem, 0))
                        .build())
                .toList();
    }

    public List<FichaHabilidade> toHabilidades(final List<FichaHabilidadeRequest> requests,
                                               final Long idFicha,
                                               final Long idOrganizacao) {
        return Optional.ofNullable(requests).orElse(List.of()).stream()
                .map(request -> FichaHabilidade.Builder.create()
                        .idOrganizacao(idOrganizacao)
                        .idFicha(idFicha)
                        .nome(request.nome())
                        .descricao(request.descricao())
                        .ordem(ObjectUtil.getIfExists(request.ordem(), ordem -> ordem, 0))
                        .build())
                .toList();
    }

    public List<FichaConquista> toConquistas(final List<FichaConquistaRequest> requests,
                                             final Long idFicha,
                                             final Long idOrganizacao) {
        return Optional.ofNullable(requests).orElse(List.of()).stream()
                .map(request -> FichaConquista.Builder.create()
                        .idOrganizacao(idOrganizacao)
                        .idFicha(idFicha)
                        .tipo(request.tipo())
                        .nome(request.nome())
                        .imagem(request.imagem())
                        .dataConquista(request.dataConquista())
                        .ordem(ObjectUtil.getIfExists(request.ordem(), ordem -> ordem, 0))
                        .build())
                .toList();
    }

    public List<FichaPokemon> toPokemons(final List<FichaPokemonRequest> requests,
                                         final Long idFicha,
                                         final Long idOrganizacao) {
        return Optional.ofNullable(requests).orElse(List.of()).stream()
                .map(request -> FichaPokemon.Builder.create()
                        .idOrganizacao(idOrganizacao)
                        .idFicha(idFicha)
                        .box(request.box())
                        .pokebola(request.pokebola())
                        .apelido(request.apelido())
                        .especie(request.especie())
                        .sprite(request.sprite())
                        .genero(request.genero())
                        .sobre(request.sobre())
                        .ability(request.ability())
                        .feature(request.feature())
                        .mecanica(request.mecanica())
                        .nature(request.nature())
                        .holdItem(request.holdItem())
                        .happinessAtual(request.happinessAtual())
                        .happinessMax(request.happinessMax())
                        .combo(request.combo())
                        .miniUpgrade(request.miniUpgrade())
                        .slotUpgrade(request.slotUpgrade())
                        .hp(request.hp())
                        .atk(request.atk())
                        .def(request.def())
                        .satk(request.satk())
                        .sdef(request.sdef())
                        .speed(request.speed())
                        .pwr(request.pwr())
                        .stm(request.stm())
                        .skl(request.skl())
                        .jmp(request.jmp())
                        .contestSpeed(request.contestSpeed())
                        .ordem(ObjectUtil.getIfExists(request.ordem(), ordem -> ordem, 0))
                        .build())
                .toList();
    }

    public List<FichaPokemonMovimento> toMovimentos(final FichaPokemonRequest request,
                                                    final Long idFicha,
                                                    final Long idOrganizacao,
                                                    final Long idPokemon) {
        return Optional.ofNullable(request.movimentos()).orElse(List.of()).stream()
                .map(movimento -> toMovimento(movimento, idFicha, idOrganizacao, idPokemon))
                .toList();
    }

    public List<FichaItem> toItens(final List<FichaItemRequest> requests,
                                   final Long idFicha,
                                   final Long idOrganizacao) {
        return Optional.ofNullable(requests).orElse(List.of()).stream()
                .map(request -> FichaItem.Builder.create()
                        .idOrganizacao(idOrganizacao)
                        .idFicha(idFicha)
                        .categoria(request.categoria())
                        .codigo(request.codigo())
                        .icone(request.icone())
                        .nome(request.nome())
                        .quantidade(ObjectUtil.getIfExists(request.quantidade(), quantidade -> quantidade, 0))
                        .descricao(request.descricao())
                        .ordem(ObjectUtil.getIfExists(request.ordem(), ordem -> ordem, 0))
                        .build())
                .toList();
    }

    public List<FichaRegistro> toRegistros(final List<FichaRegistroRequest> requests,
                                           final Long idFicha,
                                           final Long idOrganizacao) {
        return Optional.ofNullable(requests).orElse(List.of()).stream()
                .map(request -> FichaRegistro.Builder.create()
                        .idOrganizacao(idOrganizacao)
                        .idFicha(idFicha)
                        .tipoMovimento(request.tipoMovimento())
                        .descricao(request.descricao())
                        .dataRegistro(request.dataRegistro())
                        .registradoEm(Optional.ofNullable(request.registradoEm()).orElseGet(Instant::now))
                        .registradoPor(request.registradoPor())
                        .ordem(ObjectUtil.getIfExists(request.ordem(), ordem -> ordem, 0))
                        .build())
                .toList();
    }

    public FichaResumoResponse toResumo(final Ficha ficha, final List<FichaPokemon> pokemonsEquipe) {
        return new FichaResumoResponse(
                ficha.getId(),
                ficha.getIdOrganizacao(),
                ficha.getNome(),
                ficha.getClassePersonagem(),
                ficha.getOcupacao(),
                ficha.getPlayer(),
                ficha.getPhotoplayer(),
                ficha.getAvatar(),
                Optional.ofNullable(pokemonsEquipe).orElse(List.of()).stream()
                        .map(pokemon -> new FichaPokemonResumoResponse(
                                pokemon.getApelido(),
                                pokemon.getEspecie(),
                                pokemon.getSprite(),
                                pokemon.getMecanica(),
                                pokemon.getOrdem()
                        ))
                        .toList(),
                ficha.getCreatedAt(),
                ficha.getUpdatedAt()
        );
    }

    public FichaResponse toResponse(final Ficha ficha, final FichaDetalhes detalhes) {
        return new FichaResponse(
                ficha.getId(),
                ficha.getIdOrganizacao(),
                ficha.getNome(),
                ficha.getFrase(),
                ficha.getIdade(),
                ficha.getNaturalidade(),
                ficha.getClassePersonagem(),
                ficha.getAlturaCm(),
                ficha.getPesoKg(),
                ficha.getTipoFisico(),
                ficha.getIndole(),
                ficha.getRanking(),
                ficha.getOcupacao(),
                ficha.getReputacao(),
                ficha.getDinheiro(),
                ficha.getPontosVida(),
                ficha.getEquipe(),
                ficha.getPontos(),
                ficha.getMiniUpgrade(),
                ficha.getSlotUpgrade(),
                ficha.getCorTema(),
                ficha.getPhotoplayer(),
                ficha.getBanner(),
                ficha.getAvatar(),
                ficha.getPlayer(),
                ficha.getBiografia(),
                ficha.getAnotacoes(),
                detalhes.relacionados().stream().map(this::toResponse).toList(),
                detalhes.habilidades().stream().map(this::toResponse).toList(),
                detalhes.conquistas().stream().map(this::toResponse).toList(),
                detalhes.pokemons().stream()
                        .map(pokemon -> toResponse(pokemon, detalhes.movimentosPorPokemon()))
                        .toList(),
                detalhes.itens().stream().map(this::toResponse).toList(),
                detalhes.registros().stream().map(this::toResponse).toList(),
                ficha.getCreatedAt(),
                ficha.getCreatedBy(),
                ficha.getUpdatedAt(),
                ficha.getUpdatedBy()
        );
    }

    private FichaPokemonMovimento toMovimento(final FichaPokemonMovimentoRequest request,
                                             final Long idFicha,
                                             final Long idOrganizacao,
                                             final Long idPokemon) {
        return FichaPokemonMovimento.Builder.create()
                .idOrganizacao(idOrganizacao)
                .idFicha(idFicha)
                .idPokemon(idPokemon)
                .nome(request.nome())
                .categoria(request.categoria())
                .tipo(request.tipo())
                .style(request.style())
                .poder(request.poder())
                .accuracy(request.accuracy())
                .ordem(ObjectUtil.getIfExists(request.ordem(), ordem -> ordem, 0))
                .build();
    }

    private FichaRelacionadoResponse toResponse(final FichaRelacionado relacionado) {
        return new FichaRelacionadoResponse(
                relacionado.getId(),
                relacionado.getNome(),
                relacionado.getRelacao(),
                relacionado.getImagem(),
                relacionado.getHistoria(),
                relacionado.getOrdem()
        );
    }

    private FichaHabilidadeResponse toResponse(final FichaHabilidade habilidade) {
        return new FichaHabilidadeResponse(
                habilidade.getId(),
                habilidade.getNome(),
                habilidade.getDescricao(),
                habilidade.getOrdem()
        );
    }

    private FichaConquistaResponse toResponse(final FichaConquista conquista) {
        return new FichaConquistaResponse(
                conquista.getId(),
                conquista.getTipo(),
                conquista.getNome(),
                conquista.getImagem(),
                conquista.getDataConquista(),
                conquista.getOrdem()
        );
    }

    private FichaPokemonResponse toResponse(final FichaPokemon pokemon,
                                            final Map<Long, List<FichaPokemonMovimento>> movimentosPorPokemon) {
        final List<FichaPokemonMovimentoResponse> movimentos = Optional
                .ofNullable(movimentosPorPokemon.get(pokemon.getId()))
                .orElse(List.of())
                .stream()
                .map(this::toResponse)
                .toList();
        return new FichaPokemonResponse(
                pokemon.getId(),
                pokemon.getBox(),
                pokemon.getPokebola(),
                pokemon.getApelido(),
                pokemon.getEspecie(),
                pokemon.getSprite(),
                pokemon.getGenero(),
                pokemon.getSobre(),
                pokemon.getAbility(),
                pokemon.getFeature(),
                pokemon.getMecanica(),
                pokemon.getNature(),
                pokemon.getHoldItem(),
                pokemon.getHappinessAtual(),
                pokemon.getHappinessMax(),
                pokemon.getCombo(),
                pokemon.getMiniUpgrade(),
                pokemon.getSlotUpgrade(),
                pokemon.getHp(),
                pokemon.getAtk(),
                pokemon.getDef(),
                pokemon.getSatk(),
                pokemon.getSdef(),
                pokemon.getSpeed(),
                pokemon.getPwr(),
                pokemon.getStm(),
                pokemon.getSkl(),
                pokemon.getJmp(),
                pokemon.getContestSpeed(),
                pokemon.getOrdem(),
                movimentos
        );
    }

    private FichaPokemonMovimentoResponse toResponse(final FichaPokemonMovimento movimento) {
        return new FichaPokemonMovimentoResponse(
                movimento.getId(),
                movimento.getNome(),
                movimento.getCategoria(),
                movimento.getTipo(),
                movimento.getStyle(),
                movimento.getPoder(),
                movimento.getAccuracy(),
                movimento.getOrdem()
        );
    }

    private FichaItemResponse toResponse(final FichaItem item) {
        return new FichaItemResponse(
                item.getId(),
                item.getCategoria(),
                item.getCodigo(),
                item.getIcone(),
                item.getNome(),
                item.getQuantidade(),
                item.getDescricao(),
                item.getOrdem()
        );
    }

    private FichaRegistroResponse toResponse(final FichaRegistro registro) {
        return new FichaRegistroResponse(
                registro.getId(),
                registro.getTipoMovimento(),
                registro.getDescricao(),
                registro.getDataRegistro(),
                registro.getRegistradoEm(),
                registro.getRegistradoPor(),
                registro.getOrdem()
        );
    }
}
