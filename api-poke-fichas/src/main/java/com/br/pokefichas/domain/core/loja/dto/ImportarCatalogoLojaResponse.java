package com.br.pokefichas.domain.core.loja.dto;

public record ImportarCatalogoLojaResponse(
        int importados,
        int atualizados,
        int removidos,
        int ignorados
) { }
