package com.br.pokefichas.domain.core.ficha.dto;

public record FichaTimelineEntryResponse(
        Long id,
        String secao,
        String periodo,
        String titulo,
        String subtitulo,
        String conteudo,
        String cor,
        Integer ordem
) {
}
