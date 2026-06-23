UPDATE fichas
SET cor_tema = '#aeb5bf'
WHERE cor_tema IS NULL
   OR LOWER(TRIM(cor_tema)) IN ('#2f6f55', '#586a9b');
