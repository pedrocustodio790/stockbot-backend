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

CREATE TABLE historico (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    tipo VARCHAR(50) NOT NULL,
    quantidade INT NOT NULL,
    usuario VARCHAR(255) NOT NULL,
    data_hora TIMESTAMP NOT NULL,
    codigo_movimentacao VARCHAR(255) NOT NULL UNIQUE,
    componente_id BIGINT NOT NULL,
    CONSTRAINT fk_historico_componente FOREIGN KEY (componente_id) REFERENCES componentes(id)
);

CREATE TABLE requisicao (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    data_requisicao TIMESTAMP,
    status VARCHAR(255),
    componente_id BIGINT NOT NULL,
    CONSTRAINT fk_requisicao_componente FOREIGN KEY (componente_id) REFERENCES componentes(id)
);