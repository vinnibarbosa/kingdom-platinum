package com.br.pokefichas.domain.core.integracao.repository;

import com.br.pokefichas.commons.persistence.JpaRepository;
import com.br.pokefichas.domain.core.integracao.model.IntegracaoAutorizacao;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.br.pokefichas.domain.core.integracao.model.QIntegracaoAutorizacao.integracaoAutorizacao;

@Component
public class IntegracaoAutorizacaoRepository {

    private final JpaRepository repository;

    public IntegracaoAutorizacaoRepository(final JpaRepository repository) {
        this.repository = repository;
    }

    public IntegracaoAutorizacao save(final IntegracaoAutorizacao authorization) {
        return repository.saveWithoutContext(authorization);
    }

    public Optional<IntegracaoAutorizacao> findForUpdateByHash(final String codeHash) {
        return Optional.ofNullable(repository.query(IntegracaoAutorizacao.class, true)
                .where(integracaoAutorizacao.codigoHash.eq(codeHash))
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .fetchOne());
    }
}
