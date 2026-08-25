ALTER TABLE fichas
    DROP CONSTRAINT IF EXISTS uk_fichas_organizacao_nome;

CREATE UNIQUE INDEX uk_fichas_usuario_nome
    ON fichas (id_usuario, LOWER(TRIM(nome)))
    WHERE npc = FALSE AND id_usuario IS NOT NULL;

CREATE UNIQUE INDEX uk_fichas_npc_organizacao_nome
    ON fichas (id_organizacao, LOWER(TRIM(nome)))
    WHERE npc = TRUE;
