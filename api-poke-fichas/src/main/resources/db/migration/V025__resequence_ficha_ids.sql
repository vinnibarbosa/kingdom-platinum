-- Reorganiza os IDs reais preservando todos os dados relacionados.
-- A ordem solicitada fica Lazzari #1, Zach #2 e Lincoln Tu Perie #3.
CREATE TEMP TABLE ficha_id_remap ON COMMIT DROP AS
SELECT
    id AS old_id,
    ROW_NUMBER() OVER (
        ORDER BY
            CASE LOWER(TRIM(nome))
                WHEN 'lazzari' THEN 1
                WHEN 'zach' THEN 2
                WHEN 'lincoln tu perie' THEN 3
                ELSE 4
            END,
            created_at,
            id
    )::BIGINT AS new_id
FROM fichas;

ALTER TABLE ficha_relacionados DROP CONSTRAINT fk_ficha_relacionados_ficha;
ALTER TABLE ficha_habilidades DROP CONSTRAINT fk_ficha_habilidades_ficha;
ALTER TABLE ficha_conquistas DROP CONSTRAINT fk_ficha_conquistas_ficha;
ALTER TABLE ficha_pokemons DROP CONSTRAINT fk_ficha_pokemons_ficha;
ALTER TABLE ficha_pokemon_movimentos DROP CONSTRAINT fk_ficha_pokemon_movimentos_ficha;
ALTER TABLE ficha_itens DROP CONSTRAINT fk_ficha_itens_ficha;
ALTER TABLE ficha_registros DROP CONSTRAINT fk_ficha_registros_ficha;
ALTER TABLE ficha_historicos DROP CONSTRAINT fk_ficha_historicos_ficha;

UPDATE ficha_relacionados SET id_ficha = -id_ficha;
UPDATE ficha_habilidades SET id_ficha = -id_ficha;
UPDATE ficha_conquistas SET id_ficha = -id_ficha;
UPDATE ficha_pokemons SET id_ficha = -id_ficha;
UPDATE ficha_pokemon_movimentos SET id_ficha = -id_ficha;
UPDATE ficha_itens SET id_ficha = -id_ficha;
UPDATE ficha_registros SET id_ficha = -id_ficha;
UPDATE ficha_historicos SET id_ficha = -id_ficha;
UPDATE fichas SET id = -id;

UPDATE fichas f
SET id = remap.new_id
FROM ficha_id_remap remap
WHERE f.id = -remap.old_id;

UPDATE ficha_relacionados child SET id_ficha = remap.new_id FROM ficha_id_remap remap WHERE child.id_ficha = -remap.old_id;
UPDATE ficha_habilidades child SET id_ficha = remap.new_id FROM ficha_id_remap remap WHERE child.id_ficha = -remap.old_id;
UPDATE ficha_conquistas child SET id_ficha = remap.new_id FROM ficha_id_remap remap WHERE child.id_ficha = -remap.old_id;
UPDATE ficha_pokemons child SET id_ficha = remap.new_id FROM ficha_id_remap remap WHERE child.id_ficha = -remap.old_id;
UPDATE ficha_pokemon_movimentos child SET id_ficha = remap.new_id FROM ficha_id_remap remap WHERE child.id_ficha = -remap.old_id;
UPDATE ficha_itens child SET id_ficha = remap.new_id FROM ficha_id_remap remap WHERE child.id_ficha = -remap.old_id;
UPDATE ficha_registros child SET id_ficha = remap.new_id FROM ficha_id_remap remap WHERE child.id_ficha = -remap.old_id;
UPDATE ficha_historicos child SET id_ficha = remap.new_id FROM ficha_id_remap remap WHERE child.id_ficha = -remap.old_id;

ALTER TABLE ficha_relacionados ADD CONSTRAINT fk_ficha_relacionados_ficha FOREIGN KEY (id_ficha) REFERENCES fichas(id) ON DELETE CASCADE;
ALTER TABLE ficha_habilidades ADD CONSTRAINT fk_ficha_habilidades_ficha FOREIGN KEY (id_ficha) REFERENCES fichas(id) ON DELETE CASCADE;
ALTER TABLE ficha_conquistas ADD CONSTRAINT fk_ficha_conquistas_ficha FOREIGN KEY (id_ficha) REFERENCES fichas(id) ON DELETE CASCADE;
ALTER TABLE ficha_pokemons ADD CONSTRAINT fk_ficha_pokemons_ficha FOREIGN KEY (id_ficha) REFERENCES fichas(id) ON DELETE CASCADE;
ALTER TABLE ficha_pokemon_movimentos ADD CONSTRAINT fk_ficha_pokemon_movimentos_ficha FOREIGN KEY (id_ficha) REFERENCES fichas(id) ON DELETE CASCADE;
ALTER TABLE ficha_itens ADD CONSTRAINT fk_ficha_itens_ficha FOREIGN KEY (id_ficha) REFERENCES fichas(id) ON DELETE CASCADE;
ALTER TABLE ficha_registros ADD CONSTRAINT fk_ficha_registros_ficha FOREIGN KEY (id_ficha) REFERENCES fichas(id) ON DELETE CASCADE;
ALTER TABLE ficha_historicos ADD CONSTRAINT fk_ficha_historicos_ficha FOREIGN KEY (id_ficha) REFERENCES fichas(id) ON DELETE CASCADE;

SELECT setval(
    pg_get_serial_sequence('fichas', 'id'),
    COALESCE((SELECT MAX(id) FROM fichas), 0) + 1,
    false
);
