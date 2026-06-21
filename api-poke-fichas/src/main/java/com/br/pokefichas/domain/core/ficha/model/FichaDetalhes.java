package com.br.pokefichas.domain.core.ficha.model;

import java.util.List;
import java.util.Map;

public record FichaDetalhes(
        List<FichaRelacionado> relacionados,
        List<FichaHabilidade> habilidades,
        List<FichaConquista> conquistas,
        List<FichaPokemon> pokemons,
        Map<Long, List<FichaPokemonMovimento>> movimentosPorPokemon,
        List<FichaItem> itens,
        List<FichaRegistro> registros
) {
}
