CREATE TABLE leads (
    id BIGINT NOT NULL AUTO_INCREMENT,
    atualizado_em DATETIME(6) NOT NULL,
    criado_em DATETIME(6) NOT NULL,
    email VARCHAR(180) NULL,
    mensagem TEXT NULL,
    nome VARCHAR(120) NOT NULL,
    origem VARCHAR(80) NULL,
    status ENUM('NOVO', 'EM_ATENDIMENTO', 'CONVERTIDO', 'ARQUIVADO') NOT NULL,
    telefone VARCHAR(30) NOT NULL,
    imovel_id BIGINT NULL,
    PRIMARY KEY (id),
    INDEX idx_leads_status (status),
    INDEX idx_leads_criado_em (criado_em),
    INDEX idx_leads_imovel_id (imovel_id),
    CONSTRAINT fk_leads_imovel
        FOREIGN KEY (imovel_id) REFERENCES imoveis (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
