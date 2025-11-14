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
-- (Traduza 'AUTO_INCREMENT' para 'GENERATED ALWAYS AS IDENTITY' em TODAS as tabelas do V1)