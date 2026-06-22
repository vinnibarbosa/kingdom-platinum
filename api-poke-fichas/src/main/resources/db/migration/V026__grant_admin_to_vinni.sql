UPDATE usuarios
SET perfil = 'A'
WHERE LOWER(TRIM(username)) = 'vinni';
