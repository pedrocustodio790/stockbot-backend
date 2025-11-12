-- V3__add_fields_to_requisicao.sql

-- Adiciona as colunas que faltavam na tabela de requisição
ALTER TABLE requisicao
    ADD COLUMN quantidade INT NOT NULL DEFAULT 1,
    ADD COLUMN usuario_id BIGINT NULL, -- NULL por enquanto, vamos arrumar
    ADD COLUMN observacao TEXT NULL;

-- Adiciona a chave estrangeira para o usuário
-- (Assumindo que sua tabela de usuários se chama 'usuarios')
ALTER TABLE requisicao
    ADD CONSTRAINT fk_requisicao_usuario
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id);

-- (Opcional, mas recomendado) Atualiza o 'usuario_id' para não ser nulo
-- ALTER TABLE requisicao MODIFY COLUMN usuario_id BIGINT NOT NULL;