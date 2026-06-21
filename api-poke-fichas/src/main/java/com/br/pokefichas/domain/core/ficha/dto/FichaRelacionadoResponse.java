package com.br.pokefichas.domain.core.ficha.dto;

public record FichaRelacionadoResponse(
        Long id,
        String nome,
        String relacao,
        String imagem,
        String historia,
        Integer ordem
) {
}
