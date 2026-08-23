ALTER TABLE ficha_historicos
    DROP CONSTRAINT IF EXISTS ck_ficha_historicos_acao;

ALTER TABLE ficha_historicos
    ADD CONSTRAINT ck_ficha_historicos_acao
        CHECK (acao IN ('ADICIONADO', 'REMOVIDO', 'ALTERADO', 'COMPRA'));
