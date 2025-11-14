ALTER TABLE componentes
MODIFY COLUMN nivel_minimo_estoque INT NOT NULL DEFAULT 5;

UPDATE componentes
SET nivel_minimo_estoque = 5
WHERE nivel_minimo_estoque = 0;