package com.br.pokefichas.domain.core.ficha.dto;

import java.time.Instant;
import java.util.List;

public record FichaResumoResponse(
        Long id,
        Long idOrganizacao,
        String nome,
        String classePersonagem,
        String ocupacao,
        String player,
        String photoplayer,
        String avatar,
        List<FichaPokemonResumoResponse> pokemonsEquipe,
        Instant createdAt,
        Instant updatedAt
) {
}
