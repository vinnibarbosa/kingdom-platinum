package com.br.pokefichas.domain.core.loja.usecase;

import com.br.pokefichas.commons.useraccess.UserAccess;
import com.br.pokefichas.domain.core.ficha.dto.FichaItemResponse;
import com.br.pokefichas.domain.core.ficha.model.Ficha;
import com.br.pokefichas.domain.core.ficha.model.FichaItem;
import com.br.pokefichas.domain.core.ficha.repository.FichaQuery;
import com.br.pokefichas.domain.core.loja.dto.FichaCompraResponse;
import com.br.pokefichas.domain.core.loja.dto.LojaItemResponse;
import com.br.pokefichas.domain.core.loja.model.LojaMapper;
import com.br.pokefichas.domain.core.loja.repository.LojaItemQuery;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class ListarLojaUseCase {
    private final LojaItemQuery lojaQuery;
    private final LojaMapper mapper;
    private final FichaQuery fichaQuery;
    private final UserAccess userAccess;

    public ListarLojaUseCase(final LojaItemQuery lojaQuery, final LojaMapper mapper,
                             final FichaQuery fichaQuery, final UserAccess userAccess) {
        this.lojaQuery = lojaQuery; this.mapper = mapper; this.fichaQuery = fichaQuery; this.userAccess = userAccess;
    }

    @Transactional(readOnly = true)
    public List<LojaItemResponse> ativos() { return lojaQuery.findAtivos().stream().map(mapper::toResponse).toList(); }

    @Transactional(readOnly = true)
    public List<LojaItemResponse> todos() { return lojaQuery.findTodos().stream().map(mapper::toResponse).toList(); }

    @Transactional(readOnly = true)
    public List<FichaCompraResponse> fichasDoUsuario() {
        final Long idUsuario = userAccess.getId().orElseThrow();
        return fichaQuery.findByUsuario(idUsuario).stream()
                .map(ficha -> new FichaCompraResponse(ficha.getId(), ficha.getNome(), ficha.getDinheiro(), ficha.getCorTema()))
                .toList();
    }
}
