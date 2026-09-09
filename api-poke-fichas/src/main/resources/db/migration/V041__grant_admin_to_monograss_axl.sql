UPDATE usuarios
SET perfil = 'A'
WHERE LOWER(BTRIM(username)) = 'monograss.axl'
  AND perfil <> 'A';
