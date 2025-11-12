-- V4__create_pedidos_compra.sql

CREATE TABLE pedidos_compra (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome_item VARCHAR(255) NOT NULL,
    quantidade INT NOT NULL,
    justificativa TEXT NULL,
    status VARCHAR(50) NOT NULL, -- PENDENTE, APROVADO, RECUSADO
    data_pedido TIMESTAMP NOT NULL,
    usuario_id BIGINT NOT NULL,

    CONSTRAINT fk_pedidocompra_usuario
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);