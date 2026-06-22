CREATE UNIQUE INDEX IF NOT EXISTS uk_usuarios_username_normalized
    ON usuarios (LOWER(BTRIM(username)));
