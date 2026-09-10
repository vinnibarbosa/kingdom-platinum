ALTER TABLE loja_itens
    ADD COLUMN gerenciado_catalogo BOOLEAN NOT NULL DEFAULT FALSE;

-- Os itens existentes foram criados pela importacao usada ate esta versao.
-- Novos itens criados manualmente permanecem com FALSE e nao sao removidos pela sincronizacao.
UPDATE loja_itens
SET gerenciado_catalogo = TRUE;
