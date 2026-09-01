package com.lorenzo.imobiliaria_api.imovel.dto;

import com.lorenzo.imobiliaria_api.imovel.Imovel;
import com.lorenzo.imobiliaria_api.imovel.StatusImovel;
import com.lorenzo.imobiliaria_api.imovel.TipoImovel;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ImovelResponse(
        Long id,
        String titulo,
        String descricao,
        BigDecimal preco,
        TipoImovel tipo,
        String cidade,
        String bairro,
        String endereco,
        Integer quartos,
        Integer banheiros,
        Integer vagas,
        BigDecimal area,
        StatusImovel status,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm
) {

    public static ImovelResponse fromEntity(Imovel imovel) {
        return new ImovelResponse(
                imovel.getId(),
                imovel.getTitulo(),
                imovel.getDescricao(),
                imovel.getPreco(),
                imovel.getTipo(),
                imovel.getCidade(),
                imovel.getBairro(),
                imovel.getEndereco(),
                imovel.getQuartos(),
                imovel.getBanheiros(),
                imovel.getVagas(),
                imovel.getArea(),
                imovel.getStatus(),
                imovel.getCriadoEm(),
                imovel.getAtualizadoEm()
        );
    }
}
