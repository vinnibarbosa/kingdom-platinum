UPDATE usuarios
SET perfil = 'A'
WHERE LOWER(TRIM(username)) IN ('luksnmds', 'ricky');
