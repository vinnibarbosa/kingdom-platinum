UPDATE usuarios
SET perfil = 'A'
WHERE perfil <> 'A'
  AND (
      LOWER(BTRIM(username)) LIKE '%jef%'
      OR LOWER(BTRIM(nome)) LIKE '%jef%'
  );
