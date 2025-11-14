CREATE TABLE usuarios (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    email VARCHAR(255) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL
);
CREATE TABLE componentes (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    nome VARCHAR(255) NOT NULL,
    codigo_patrimonio VARCHAR(255) NOT NULL UNIQUE,
    quantidade INT NOT NULL DEFAULT 0,
    localizacao VARCHAR(255),
    categoria VARCHAR(255),
    observacoes TEXT,
    nivel_minimo_estoque INT NOT NULL DEFAULT 0
);
CREATE TABLE requisicao (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    componente_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    quantidade INT NOT NULL,
    data_requisicao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDENTE',
    observacao TEXT,
    FOREIGN KEY (componente_id) REFERENCES componentes(id),
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);
-- (Traduza 'AUTO_INCREMENT' para 'GENERATED ALWAYS AS IDENTITY' em TODAS as tabelas do V1)