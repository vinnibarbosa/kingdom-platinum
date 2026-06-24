package com.br.pokefichas.domain.core.ficha.repository;

import com.br.pokefichas.commons.persistence.JpaRepository;
import com.br.pokefichas.domain.core.ficha.model.Ficha;
import com.br.pokefichas.domain.core.ficha.model.FichaConquista;
import com.br.pokefichas.domain.core.ficha.model.FichaHabilidade;
import com.br.pokefichas.domain.core.ficha.model.FichaHistorico;
import com.br.pokefichas.domain.core.ficha.model.FichaItem;
import com.br.pokefichas.domain.core.ficha.model.FichaPokemon;
import com.br.pokefichas.domain.core.ficha.model.FichaPokemonMovimento;
import com.br.pokefichas.domain.core.ficha.model.FichaRegistro;
import com.br.pokefichas.domain.core.ficha.model.FichaRelacionado;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.br.pokefichas.domain.core.ficha.model.QFichaConquista.fichaConquista;
import static com.br.pokefichas.domain.core.ficha.model.QFichaHabilidade.fichaHabilidade;
import static com.br.pokefichas.domain.core.ficha.model.QFichaItem.fichaItem;
import static com.br.pokefichas.domain.core.ficha.model.QFichaPokemon.fichaPokemon;
import static com.br.pokefichas.domain.core.ficha.model.QFichaPokemonMovimento.fichaPokemonMovimento;
import static com.br.pokefichas.domain.core.ficha.model.QFichaRegistro.fichaRegistro;
import static com.br.pokefichas.domain.core.ficha.model.QFichaRelacionado.fichaRelacionado;

@Component
public class FichaCommand {

    private final JpaRepository repository;

    public FichaCommand(final JpaRepository repository) {
        this.repository = repository;
    }

    public Ficha save(final Ficha ficha) {
        return repository.save(ficha);
    }

    public Ficha saveWithoutContext(final Ficha ficha) {
        return repository.saveWithoutContext(ficha);
    }

    public void delete(final Ficha ficha) {
        repository.remove(ficha);
    }

    public List<FichaRelacionado> saveRelacionados(final List<FichaRelacionado> relacionados) {
        return repository.saveAll(relacionados);
    }

    public List<FichaHabilidade> saveHabilidades(final List<FichaHabilidade> habilidades) {
        return repository.saveAll(habilidades);
    }

    public List<FichaConquista> saveConquistas(final List<FichaConquista> conquistas) {
        return repository.saveAll(conquistas);
    }

    public List<FichaPokemon> savePokemons(final List<FichaPokemon> pokemons) {
        return repository.saveAll(pokemons);
    }

    public List<FichaPokemonMovimento> saveMovimentos(final List<FichaPokemonMovimento> movimentos) {
        return repository.saveAll(movimentos);
    }

    public List<FichaItem> saveItens(final List<FichaItem> itens) {
        return repository.saveAll(itens);
    }

    public List<FichaRegistro> saveRegistros(final List<FichaRegistro> registros) {
        return repository.saveAll(registros);
    }

    public List<FichaHistorico> saveHistoricos(final List<FichaHistorico> historicos) {
        return repository.saveAll(historicos);
    }

    public List<FichaRelacionado> saveRelacionadosWithoutContext(final List<FichaRelacionado> relacionados) {
        return repository.saveAllWithoutContext(relacionados);
    }

    public List<FichaHabilidade> saveHabilidadesWithoutContext(final List<FichaHabilidade> habilidades) {
        return repository.saveAllWithoutContext(habilidades);
    }

    public List<FichaConquista> saveConquistasWithoutContext(final List<FichaConquista> conquistas) {
        return repository.saveAllWithoutContext(conquistas);
    }

    public List<FichaPokemon> savePokemonsWithoutContext(final List<FichaPokemon> pokemons) {
        return repository.saveAllWithoutContext(pokemons);
    }

    public List<FichaPokemonMovimento> saveMovimentosWithoutContext(final List<FichaPokemonMovimento> movimentos) {
        return repository.saveAllWithoutContext(movimentos);
    }

    public List<FichaItem> saveItensWithoutContext(final List<FichaItem> itens) {
        return repository.saveAllWithoutContext(itens);
    }

    public List<FichaRegistro> saveRegistrosWithoutContext(final List<FichaRegistro> registros) {
        return repository.saveAllWithoutContext(registros);
    }

    public List<FichaHistorico> saveHistoricosWithoutContext(final List<FichaHistorico> historicos) {
        return repository.saveAllWithoutContext(historicos);
    }

    public void deleteDetalhesByFicha(final Long idFicha) {
        repository.delete(FichaPokemonMovimento.class, fichaPokemonMovimento.idFicha.eq(idFicha));
        repository.delete(FichaPokemon.class, fichaPokemon.idFicha.eq(idFicha));
        repository.delete(FichaRelacionado.class, fichaRelacionado.idFicha.eq(idFicha));
        repository.delete(FichaHabilidade.class, fichaHabilidade.idFicha.eq(idFicha));
        repository.delete(FichaConquista.class, fichaConquista.idFicha.eq(idFicha));
        repository.delete(FichaItem.class, fichaItem.idFicha.eq(idFicha));
        repository.delete(FichaRegistro.class, fichaRegistro.idFicha.eq(idFicha));
    }

    public void deleteDetalhesByFichaWithoutContext(final Long idFicha) {
        repository.deleteWithoutContext(FichaPokemonMovimento.class, fichaPokemonMovimento.idFicha.eq(idFicha));
        repository.deleteWithoutContext(FichaPokemon.class, fichaPokemon.idFicha.eq(idFicha));
        repository.deleteWithoutContext(FichaRelacionado.class, fichaRelacionado.idFicha.eq(idFicha));
        repository.deleteWithoutContext(FichaHabilidade.class, fichaHabilidade.idFicha.eq(idFicha));
        repository.deleteWithoutContext(FichaConquista.class, fichaConquista.idFicha.eq(idFicha));
        repository.deleteWithoutContext(FichaItem.class, fichaItem.idFicha.eq(idFicha));
        repository.deleteWithoutContext(FichaRegistro.class, fichaRegistro.idFicha.eq(idFicha));
    }
}
