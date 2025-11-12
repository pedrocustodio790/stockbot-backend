-- V1__create-initial-tables.sql

-- Tabela para os usuários do sistema, baseada na sua entidade Usuario
CREATE TABLE usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL
);

-- Tabela para os componentes do estoque, baseada na sua entidade Componente
CREATE TABLE componentes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    codigo_patrimonio VARCHAR(255) NOT NULL UNIQUE,
    quantidade INT NOT NULL DEFAULT 0,
    localizacao VARCHAR(255),
    categoria VARCHAR(255),
    observacoes TEXT, -- TEXT é melhor para observações longas
    nivel_minimo_estoque INT NOT NULL DEFAULT 0
);

-- Tabela para o histórico de movimentações, baseada na sua entidade Historico
CREATE TABLE historico (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tipo VARCHAR(50) NOT NULL, -- ENTRADA, SAIDA
    quantidade INT NOT NULL,
    usuario VARCHAR(255) NOT NULL,
    data_hora TIMESTAMP NOT NULL,
    codigo_movimentacao VARCHAR(255) NOT NULL UNIQUE,
    componente_id BIGINT NOT NULL,
    CONSTRAINT fk_historico_componente FOREIGN KEY (componente_id) REFERENCES componentes(id)
);

-- Tabela para as requisições, baseada na sua entidade Requisicao
CREATE TABLE requisicao (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    data_requisicao TIMESTAMP,
    status VARCHAR(255),
    componente_id BIGINT NOT NULL,
    CONSTRAINT fk_requisicao_componente FOREIGN KEY (componente_id) REFERENCES componentes(id)
);