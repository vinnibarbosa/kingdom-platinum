package com.br.pokefichas.domain.core.ficha.repository;

import com.br.pokefichas.commons.dto.PageRequest;
import com.br.pokefichas.commons.page.Page;
import com.br.pokefichas.commons.page.Pageable;
import com.br.pokefichas.commons.page.Sort;
import com.br.pokefichas.commons.persistence.JpaRepository;
import com.br.pokefichas.domain.core.ficha.model.Ficha;
import com.br.pokefichas.domain.core.ficha.model.FichaConquista;
import com.br.pokefichas.domain.core.ficha.model.FichaDetalhes;
import com.br.pokefichas.domain.core.ficha.model.FichaHabilidade;
import com.br.pokefichas.domain.core.ficha.model.FichaHistorico;
import com.br.pokefichas.domain.core.ficha.model.FichaItem;
import com.br.pokefichas.domain.core.ficha.model.FichaPokemon;
import com.br.pokefichas.domain.core.ficha.model.FichaPokemonMovimento;
import com.br.pokefichas.domain.core.ficha.model.FichaRegistro;
import com.br.pokefichas.domain.core.ficha.model.FichaRelacionado;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.br.pokefichas.domain.core.ficha.model.QFicha.ficha;
import static com.br.pokefichas.domain.core.ficha.model.QFichaConquista.fichaConquista;
import static com.br.pokefichas.domain.core.ficha.model.QFichaHabilidade.fichaHabilidade;
import static com.br.pokefichas.domain.core.ficha.model.QFichaHistorico.fichaHistorico;
import static com.br.pokefichas.domain.core.ficha.model.QFichaItem.fichaItem;
import static com.br.pokefichas.domain.core.ficha.model.QFichaPokemon.fichaPokemon;
import static com.br.pokefichas.domain.core.ficha.model.QFichaPokemonMovimento.fichaPokemonMovimento;
import static com.br.pokefichas.domain.core.ficha.model.QFichaRegistro.fichaRegistro;
import static com.br.pokefichas.domain.core.ficha.model.QFichaRelacionado.fichaRelacionado;

@Component
public class FichaQuery {

    private final JpaRepository repository;

    public FichaQuery(final JpaRepository repository) {
        this.repository = repository;
    }

    public Optional<Ficha> findById(final Long id) {
        return repository.findOptional(Ficha.class, id);
    }

    public Optional<Ficha> findByIdWithoutContext(final Long id) {
        return repository.findUniqueOptionalWithoutTenantFilter(Ficha.class, ficha.id.eq(id));
    }

    public List<Ficha> findAllWithoutContext() {
        return repository.findAllWithoutTenantFilter(Ficha.class);
    }

    public Page<Ficha> findAll(final PageRequest pageRequest) {
        final Pageable pageable = pageRequest.toPageable(Sort.of(ficha.nome.asc(), ficha.id.asc()));
        return repository.findAll(Ficha.class, pageable);
    }

    public Page<Ficha> findAllWithoutContext(final PageRequest pageRequest) {
        final Pageable pageable = pageRequest.toPageable(Sort.of(ficha.nome.asc(), ficha.id.asc()));
        return repository.findAll(Ficha.class, pageable, true);
    }

    public long countCurrentOrganization() {
        return repository.count(Ficha.class);
    }

    public List<FichaPokemon> findPokemonsWithoutContextByFichaIds(final List<Long> idsFicha) {
        if (idsFicha == null || idsFicha.isEmpty()) {
            return List.of();
        }
        return repository.findAllWithoutTenantFilter(
                FichaPokemon.class,
                fichaPokemon.idFicha.in(idsFicha)
                        .and(fichaPokemon.box.isNull().or(fichaPokemon.box.lower().notIn("box", "pc")))
        );
    }

    public List<FichaHistorico> findHistoricosWithoutContext(final Long idFicha) {
        return repository.findAllWithoutTenantFilter(
                        FichaHistorico.class,
                        fichaHistorico.idFicha.eq(idFicha)
                ).stream()
                .sorted((first, second) -> {
                    final int dateOrder = java.util.Comparator.nullsLast(java.util.Comparator.<java.time.Instant>reverseOrder())
                            .compare(first.getCreatedAt(), second.getCreatedAt());
                    if (dateOrder != 0) {
                        return dateOrder;
                    }
                    return java.util.Comparator.nullsLast(java.util.Comparator.<Long>reverseOrder())
                            .compare(first.getId(), second.getId());
                })
                .limit(300)
                .toList();
    }

    public boolean existsByNome(final Long idOrganizacao, final String nome, final Long id) {
        if (id == null) {
            return repository.existsWithoutTenant(
                    Ficha.class,
                    ficha.idOrganizacao.eq(idOrganizacao).and(ficha.nome.equalsIgnoreCase(nome))
            );
        }
        return repository.existsWithoutTenant(
                Ficha.class,
                ficha.idOrganizacao.eq(idOrganizacao)
                        .and(ficha.nome.equalsIgnoreCase(nome))
                        .and(ficha.id.ne(id))
        );
    }

    public FichaDetalhes findDetalhes(final Long idFicha) {
        final List<FichaRelacionado> relacionados = repository.findAll(
                FichaRelacionado.class,
                Sort.of(fichaRelacionado.ordem.asc(), fichaRelacionado.id.asc()),
                fichaRelacionado.idFicha.eq(idFicha)
        );
        final List<FichaHabilidade> habilidades = repository.findAll(
                FichaHabilidade.class,
                Sort.of(fichaHabilidade.ordem.asc(), fichaHabilidade.id.asc()),
                fichaHabilidade.idFicha.eq(idFicha)
        );
        final List<FichaConquista> conquistas = repository.findAll(
                FichaConquista.class,
                Sort.of(fichaConquista.ordem.asc(), fichaConquista.id.asc()),
                fichaConquista.idFicha.eq(idFicha)
        );
        final List<FichaPokemon> pokemons = repository.findAll(
                FichaPokemon.class,
                Sort.of(fichaPokemon.ordem.asc(), fichaPokemon.id.asc()),
                fichaPokemon.idFicha.eq(idFicha)
        );
        final List<FichaPokemonMovimento> movimentos = repository.findAll(
                FichaPokemonMovimento.class,
                Sort.of(fichaPokemonMovimento.ordem.asc(), fichaPokemonMovimento.id.asc()),
                fichaPokemonMovimento.idFicha.eq(idFicha)
        );
        final Map<Long, List<FichaPokemonMovimento>> movimentosPorPokemon = movimentos.stream()
                .collect(Collectors.groupingBy(FichaPokemonMovimento::getIdPokemon));
        final List<FichaItem> itens = repository.findAll(
                FichaItem.class,
                Sort.of(fichaItem.ordem.asc(), fichaItem.id.asc()),
                fichaItem.idFicha.eq(idFicha)
        );
        final List<FichaRegistro> registros = repository.findAll(
                FichaRegistro.class,
                Sort.of(fichaRegistro.ordem.asc(), fichaRegistro.id.asc()),
                fichaRegistro.idFicha.eq(idFicha)
        );
        return new FichaDetalhes(
                relacionados,
                habilidades,
                conquistas,
                pokemons,
                movimentosPorPokemon,
                itens,
                registros
        );
    }

    public FichaDetalhes findDetalhesWithoutContext(final Long idFicha) {
        final List<FichaRelacionado> relacionados = repository.findAllWithoutTenantFilter(
                FichaRelacionado.class,
                fichaRelacionado.idFicha.eq(idFicha)
        );
        final List<FichaHabilidade> habilidades = repository.findAllWithoutTenantFilter(
                FichaHabilidade.class,
                fichaHabilidade.idFicha.eq(idFicha)
        );
        final List<FichaConquista> conquistas = repository.findAllWithoutTenantFilter(
                FichaConquista.class,
                fichaConquista.idFicha.eq(idFicha)
        );
        final List<FichaPokemon> pokemons = repository.findAllWithoutTenantFilter(
                FichaPokemon.class,
                fichaPokemon.idFicha.eq(idFicha)
        );
        final List<FichaPokemonMovimento> movimentos = repository.findAllWithoutTenantFilter(
                FichaPokemonMovimento.class,
                fichaPokemonMovimento.idFicha.eq(idFicha)
        );
        final Map<Long, List<FichaPokemonMovimento>> movimentosPorPokemon = movimentos.stream()
                .collect(Collectors.groupingBy(FichaPokemonMovimento::getIdPokemon));
        final List<FichaItem> itens = repository.findAllWithoutTenantFilter(
                FichaItem.class,
                fichaItem.idFicha.eq(idFicha)
        );
        final List<FichaRegistro> registros = repository.findAllWithoutTenantFilter(
                FichaRegistro.class,
                fichaRegistro.idFicha.eq(idFicha)
        );
        return new FichaDetalhes(
                relacionados.stream().sorted((a, b) -> compareOrder(a.getOrdem(), a.getId(), b.getOrdem(), b.getId())).toList(),
                habilidades.stream().sorted((a, b) -> compareOrder(a.getOrdem(), a.getId(), b.getOrdem(), b.getId())).toList(),
                conquistas.stream().sorted((a, b) -> compareOrder(a.getOrdem(), a.getId(), b.getOrdem(), b.getId())).toList(),
                pokemons.stream().sorted((a, b) -> compareOrder(a.getOrdem(), a.getId(), b.getOrdem(), b.getId())).toList(),
                movimentosPorPokemon,
                itens.stream().sorted((a, b) -> compareOrder(a.getOrdem(), a.getId(), b.getOrdem(), b.getId())).toList(),
                registros.stream().sorted((a, b) -> compareOrder(a.getOrdem(), a.getId(), b.getOrdem(), b.getId())).toList()
        );
    }

    private int compareOrder(final Integer firstOrder, final Long firstId, final Integer secondOrder, final Long secondId) {
        final int order = Integer.compare(
                firstOrder == null ? Integer.MAX_VALUE : firstOrder,
                secondOrder == null ? Integer.MAX_VALUE : secondOrder
        );
        if (order != 0) {
            return order;
        }
        return Long.compare(firstId == null ? Long.MAX_VALUE : firstId, secondId == null ? Long.MAX_VALUE : secondId);
    }
}
