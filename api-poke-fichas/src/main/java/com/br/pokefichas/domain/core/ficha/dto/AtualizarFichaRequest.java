package com.br.pokefichas.domain.core.ficha.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record AtualizarFichaRequest(
        @NotBlank(message = "Nome da ficha e obrigatorio")
        @Size(max = 150)
        String nome,
        @Size(max = 255)
        String frase,
        @PositiveOrZero
        Integer idade,
        @Size(max = 120)
        String naturalidade,
        @Size(max = 80)
        String classePersonagem,
        @PositiveOrZero
        BigDecimal alturaCm,
        @PositiveOrZero
        BigDecimal pesoKg,
        @Size(max = 80)
        String tipoFisico,
        @Size(max = 80)
        String indole,
        @PositiveOrZero
        Integer ranking,
        @Size(max = 120)
        String ocupacao,
        Integer reputacao,
        @PositiveOrZero
        BigDecimal dinheiro,
        @PositiveOrZero
        Integer pontosVida,
        @Size(max = 120)
        String equipe,
        @PositiveOrZero
        Integer pontos,
        @PositiveOrZero
        Integer miniUpgrade,
        @PositiveOrZero
        Integer slotUpgrade,
        @Size(max = 24)
        String corTema,
        String photoplayer,
        @Size(max = 120)
        String avatar,
        @Size(max = 120)
        String player,
        String biografia,
        String anotacoes,
        List<@Valid FichaRelacionadoRequest> relacionados,
        List<@Valid FichaHabilidadeRequest> habilidades,
        List<@Valid FichaConquistaRequest> conquistas,
        List<@Valid FichaPokemonRequest> pokemons,
        List<@Valid FichaItemRequest> itens,
        List<@Valid FichaRegistroRequest> registros
) {
}
