ALTER TABLE usuarios ADD COLUMN dominio VARCHAR(100);
UPDATE usuarios SET dominio = 'principal' WHERE dominio IS NULL;
ALTER TABLE usuarios ALTER COLUMN dominio SET NOT NULL;
ALTER TABLE usuarios DROP CONSTRAINT usuarios_email_key; -- (O nome da constraint UNIQUE do V1)
CREATE UNIQUE INDEX uk_usuario_email_dominio ON usuarios (email, dominio);