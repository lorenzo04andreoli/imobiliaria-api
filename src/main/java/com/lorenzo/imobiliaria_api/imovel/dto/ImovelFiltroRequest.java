package com.lorenzo.imobiliaria_api.imovel.dto;

import com.lorenzo.imobiliaria_api.imovel.TipoImovel;

import java.math.BigDecimal;

public record ImovelFiltroRequest(
        String q,
        String cidade,
        String bairro,
        TipoImovel tipo,
        BigDecimal precoMin,
        BigDecimal precoMax,
        Integer quartosMin,
        Integer banheirosMin,
        Integer vagasMin
) {
}
