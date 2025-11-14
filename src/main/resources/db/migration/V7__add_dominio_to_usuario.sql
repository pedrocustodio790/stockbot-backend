-- V6__add_dominio_to_usuarios.sql
ALTER TABLE usuarios ADD COLUMN dominio VARCHAR(100);
UPDATE usuarios SET dominio = 'principal' WHERE dominio IS NULL;
ALTER TABLE usuarios ALTER COLUMN dominio SET NOT NULL;

-- Remove a constraint UNIQUE do email (nome genérico)
ALTER TABLE usuarios DROP CONSTRAINT IF EXISTS usuarios_email_key;
ALTER TABLE usuarios DROP CONSTRAINT IF EXISTS usuarios_email_key1;
ALTER TABLE usuarios DROP CONSTRAINT IF EXISTS uk_email; -- outros nomes possíveis

-- Cria novo índice único composto
CREATE UNIQUE INDEX uk_usuario_email_dominio ON usuarios (email, dominio);