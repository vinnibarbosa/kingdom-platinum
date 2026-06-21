ALTER TABLE ficha_registros
    ADD COLUMN registrado_em TIMESTAMP,
    ADD COLUMN registrado_por VARCHAR(255);

UPDATE ficha_registros
SET registrado_em = created_at,
    registrado_por = created_by
WHERE registrado_em IS NULL;

ALTER TABLE ficha_registros
    ALTER COLUMN registrado_em SET DEFAULT CURRENT_TIMESTAMP,
    ALTER COLUMN registrado_em SET NOT NULL;
