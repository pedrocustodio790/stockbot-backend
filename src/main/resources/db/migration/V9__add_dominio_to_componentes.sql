ALTER TABLE componentes ADD COLUMN dominio VARCHAR(100);
UPDATE componentes SET dominio = 'principal' WHERE dominio IS NULL;
ALTER TABLE componentes ALTER COLUMN dominio SET NOT NULL;