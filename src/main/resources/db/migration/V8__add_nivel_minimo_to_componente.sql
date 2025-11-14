ALTER TABLE componentes ALTER COLUMN nivel_minimo_estoque SET DEFAULT 5;
UPDATE componentes SET nivel_minimo_estoque = 5 WHERE nivel_minimo_estoque = 0;