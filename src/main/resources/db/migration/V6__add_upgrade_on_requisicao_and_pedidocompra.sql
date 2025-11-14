-- V5__add_audit_fields.sql

-- Adiciona os campos de auditoria na tabela 'pedidos_compra'
ALTER TABLE pedidos_compra
ADD COLUMN aprovador_id BIGINT NULL,
ADD COLUMN data_acao TIMESTAMP NULL,  -- ← CORREÇÃO AQUI!
ADD COLUMN motivo_acao VARCHAR(1000) NULL;

-- Adiciona a chave estrangeira para o aprovador
ALTER TABLE pedidos_compra
ADD CONSTRAINT fk_pedidos_compra_aprovador
    FOREIGN KEY (aprovador_id) REFERENCES usuarios(id);

-- FAÇA O MESMO PARA A TABELA 'requisicao'
ALTER TABLE requisicao
ADD COLUMN aprovador_id BIGINT NULL,
ADD COLUMN data_acao TIMESTAMP NULL,  -- ← CORREÇÃO AQUI!
ADD COLUMN motivo_acao VARCHAR(1000) NULL;

-- Adiciona a chave estrangeira para o aprovador
ALTER TABLE requisicao
ADD CONSTRAINT fk_requisicao_aprovador
    FOREIGN KEY (aprovador_id) REFERENCES usuarios(id);