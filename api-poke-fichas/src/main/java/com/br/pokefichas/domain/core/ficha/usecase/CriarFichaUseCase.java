package com.br.pokefichas.domain.core.ficha.usecase;

import com.br.pokefichas.commons.organizacao.OrganizacaoContext;
import com.br.pokefichas.commons.exception.BusinessException;
import com.br.pokefichas.domain.core.ficha.dto.CriarFichaRequest;
import com.br.pokefichas.domain.core.ficha.dto.FichaResponse;
import com.br.pokefichas.domain.core.ficha.model.Ficha;
import com.br.pokefichas.domain.core.ficha.model.FichaDetalhes;
import com.br.pokefichas.domain.core.ficha.model.FichaMapper;
import com.br.pokefichas.domain.core.ficha.repository.FichaCommand;
import com.br.pokefichas.domain.core.ficha.repository.FichaQuery;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CriarFichaUseCase {

    private static final long MAX_FICHAS_POR_CONTA = 2;

    private final FichaCommand command;
    private final FichaQuery query;
    private final FichaMapper mapper;
    private final FichaDetalhesWriter detalhesWriter;
    private final FichaHistoricoWriter historicoWriter;
    private final OrganizacaoContext organizacaoContext;

    public CriarFichaUseCase(final FichaCommand command,
                             final FichaQuery query,
                             final FichaMapper mapper,
                             final FichaDetalhesWriter detalhesWriter,
                             final FichaHistoricoWriter historicoWriter,
                             final OrganizacaoContext organizacaoContext) {
        this.command = command;
        this.query = query;
        this.mapper = mapper;
        this.detalhesWriter = detalhesWriter;
        this.historicoWriter = historicoWriter;
        this.organizacaoContext = organizacaoContext;
    }

    @Transactional
    public FichaResponse handle(final CriarFichaRequest request) {
        final Long idOrganizacao = organizacaoContext.getRequiredOrganizacaoId();
        if (query.countCurrentOrganization() >= MAX_FICHAS_POR_CONTA) {
            throw new BusinessException(
                    "Cada conta pode criar no maximo 2 fichas.",
                    "FICHA_LIMIT_REACHED"
            );
        }
        final Ficha saved = command.save(mapper.toEntity(request, idOrganizacao));
        detalhesWriter.save(request, saved.getId(), idOrganizacao);
        final FichaDetalhes detalhes = query.findDetalhes(saved.getId());
        final FichaResponse response = mapper.toResponse(saved, detalhes);
        historicoWriter.recordCreation(response);
        return response;
    }
}
