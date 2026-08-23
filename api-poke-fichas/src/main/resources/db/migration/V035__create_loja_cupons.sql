CREATE TABLE loja_cupons (
    id BIGSERIAL PRIMARY KEY,
    id_organizacao BIGINT NOT NULL,
    codigo VARCHAR(40) NOT NULL,
    percentual INTEGER NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_loja_cupons_percentual CHECK (percentual BETWEEN 1 AND 100),
    CONSTRAINT uk_loja_cupons_organizacao_codigo UNIQUE (id_organizacao, codigo)
);

CREATE INDEX idx_loja_cupons_organizacao_ativo ON loja_cupons (id_organizacao, ativo);
