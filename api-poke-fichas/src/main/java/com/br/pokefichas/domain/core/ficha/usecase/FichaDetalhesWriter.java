package com.br.pokefichas.domain.core.ficha.usecase;

import com.br.pokefichas.domain.core.ficha.dto.AtualizarFichaRequest;
import com.br.pokefichas.domain.core.ficha.dto.CriarFichaRequest;
import com.br.pokefichas.domain.core.ficha.dto.FichaPokemonRequest;
import com.br.pokefichas.domain.core.ficha.model.FichaMapper;
import com.br.pokefichas.domain.core.ficha.model.FichaPokemon;
import com.br.pokefichas.domain.core.ficha.model.FichaPokemonMovimento;
import com.br.pokefichas.domain.core.ficha.repository.FichaCommand;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

@Component
public class FichaDetalhesWriter {

    private final FichaCommand command;
    private final FichaMapper mapper;

    public FichaDetalhesWriter(final FichaCommand command, final FichaMapper mapper) {
        this.command = command;
        this.mapper = mapper;
    }

    public void save(final CriarFichaRequest request, final Long idFicha, final Long idOrganizacao) {
        command.saveRelacionados(mapper.toRelacionados(request.relacionados(), idFicha, idOrganizacao));
        command.saveHabilidades(mapper.toHabilidades(request.habilidades(), idFicha, idOrganizacao));
        command.saveConquistas(mapper.toConquistas(request.conquistas(), idFicha, idOrganizacao));
        savePokemons(request.pokemons(), idFicha, idOrganizacao);
        command.saveItens(mapper.toItens(request.itens(), idFicha, idOrganizacao));
        command.saveRegistros(mapper.toRegistros(request.registros(), idFicha, idOrganizacao));
    }

    public void replace(final AtualizarFichaRequest request, final Long idFicha, final Long idOrganizacao) {
        command.deleteDetalhesByFicha(idFicha);
        command.saveRelacionados(mapper.toRelacionados(request.relacionados(), idFicha, idOrganizacao));
        command.saveHabilidades(mapper.toHabilidades(request.habilidades(), idFicha, idOrganizacao));
        command.saveConquistas(mapper.toConquistas(request.conquistas(), idFicha, idOrganizacao));
        savePokemons(request.pokemons(), idFicha, idOrganizacao);
        command.saveItens(mapper.toItens(request.itens(), idFicha, idOrganizacao));
        command.saveRegistros(mapper.toRegistros(request.registros(), idFicha, idOrganizacao));
    }

    private void savePokemons(final List<FichaPokemonRequest> requests,
                              final Long idFicha,
                              final Long idOrganizacao) {
        final List<FichaPokemonRequest> pokemonRequests = Optional.ofNullable(requests).orElse(List.of());
        final List<FichaPokemon> savedPokemons = command.savePokemons(
                mapper.toPokemons(pokemonRequests, idFicha, idOrganizacao)
        );
        final List<FichaPokemonMovimento> movimentos = IntStream.range(0, savedPokemons.size())
                .mapToObj(index -> mapper.toMovimentos(
                        pokemonRequests.get(index),
                        idFicha,
                        idOrganizacao,
                        savedPokemons.get(index).getId()
                ))
                .flatMap(List::stream)
                .toList();
        command.saveMovimentos(movimentos);
    }
}
