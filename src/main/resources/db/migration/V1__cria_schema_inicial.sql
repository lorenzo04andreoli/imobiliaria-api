CREATE TABLE imoveis (
    id BIGINT NOT NULL AUTO_INCREMENT,
    area DECIMAL(10, 2) NULL,
    atualizado_em DATETIME(6) NOT NULL,
    bairro VARCHAR(100) NOT NULL,
    banheiros INT NULL,
    cidade VARCHAR(100) NOT NULL,
    criado_em DATETIME(6) NOT NULL,
    descricao TEXT NOT NULL,
    endereco VARCHAR(180) NULL,
    preco DECIMAL(12, 2) NOT NULL,
    quartos INT NULL,
    status ENUM('INATIVO', 'PUBLICADO', 'RASCUNHO', 'VENDIDO') NOT NULL,
    tipo ENUM('APARTAMENTO', 'CASA', 'CHACARA', 'COMERCIAL', 'OUTRO', 'TERRENO') NOT NULL,
    titulo VARCHAR(150) NOT NULL,
    vagas INT NULL,
    PRIMARY KEY (id),
    CONSTRAINT imoveis_chk_banheiros CHECK (banheiros >= 0),
    CONSTRAINT imoveis_chk_quartos CHECK (quartos >= 0),
    CONSTRAINT imoveis_chk_vagas CHECK (vagas >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE imagens_imovel (
    id BIGINT NOT NULL AUTO_INCREMENT,
    capa BIT(1) NOT NULL,
    ordem INT NOT NULL,
    url VARCHAR(500) NOT NULL,
    imovel_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_imagens_imovel_imovel_id (imovel_id),
    CONSTRAINT fk_imagens_imovel_imovel
        FOREIGN KEY (imovel_id) REFERENCES imoveis (id),
    CONSTRAINT imagens_imovel_chk_ordem CHECK (ordem >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE usuarios (
    id BIGINT NOT NULL AUTO_INCREMENT,
    ativo BIT(1) NOT NULL,
    email VARCHAR(180) NOT NULL,
    nome VARCHAR(120) NOT NULL,
    role ENUM('ADMIN') NOT NULL,
    senha VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_usuarios_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
