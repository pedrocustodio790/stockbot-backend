ALTER TABLE usuarios ADD COLUMN dominio VARCHAR(100);

UPDATE usuarios SET dominio = 'principal' WHERE dominio IS NULL;

ALTER TABLE usuarios MODIFY COLUMN dominio VARCHAR(100) NOT NULL;

/* 4. Remove a chave única antiga (COM O NOME CORRETO) */
ALTER TABLE usuarios DROP INDEX email;

/* 5. Cria a nova chave única combinada */
CREATE UNIQUE INDEX uk_usuario_email_dominio ON usuarios (email, dominio);