-- V9__fix_unique_componentes.sql

-- 1. Remove a restrição de que o código deve ser único no mundo todo
-- (O nome da constraint geralmente é 'componentes_codigo_patrimonio_key' no Postgres)
ALTER TABLE componentes DROP CONSTRAINT IF EXISTS componentes_codigo_patrimonio_key;
-- Tenta remover com outro nome caso o banco tenha gerado diferente
ALTER TABLE componentes DROP CONSTRAINT IF EXISTS uk_codigo_patrimonio;

-- 2. Cria a nova regra: O código só precisa ser único DENTRO DA EMPRESA (domínio)
CREATE UNIQUE INDEX uk_componente_codigo_dominio
ON componentes (codigo_patrimonio, dominio);