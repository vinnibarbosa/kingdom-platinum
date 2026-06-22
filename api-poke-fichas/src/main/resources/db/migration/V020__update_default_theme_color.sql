UPDATE fichas
SET cor_tema = '#586a9b'
WHERE cor_tema IS NULL
   OR LOWER(cor_tema) = '#2f6f55';
