package com.br.pokefichas.domain.core.ficha.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record FichaResponse(
        Long id,
        Long idOrganizacao,
        String nome,
        String frase,
        Integer idade,
        String naturalidade,
        String classePersonagem,
        BigDecimal alturaCm,
        BigDecimal pesoKg,
        String tipoFisico,
        String indole,
        Integer ranking,
        String ocupacao,
        Integer reputacao,
        BigDecimal dinheiro,
        Integer pontosVida,
        String equipe,
        Integer pontos,
        Integer miniUpgrade,
        Integer slotUpgrade,
        String corTema,
        String photoplayer,
        String banner,
        String avatar,
        String player,
        String biografia,
        String anotacoes,
        List<FichaRelacionadoResponse> relacionados,
        List<FichaHabilidadeResponse> habilidades,
        List<FichaConquistaResponse> conquistas,
        List<FichaPokemonResponse> pokemons,
        List<FichaItemResponse> itens,
        List<FichaRegistroResponse> registros,
        Instant createdAt,
        String createdBy,
        Instant updatedAt,
        String updatedBy
) {
}
