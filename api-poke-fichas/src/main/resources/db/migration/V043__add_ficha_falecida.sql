ALTER TABLE fichas ADD COLUMN falecida BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX idx_fichas_falecida ON fichas (falecida);
