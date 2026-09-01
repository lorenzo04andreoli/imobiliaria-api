package com.lorenzo.imobiliaria_api.imovel.dto;

import com.lorenzo.imobiliaria_api.imovel.StatusImovel;
import com.lorenzo.imobiliaria_api.imovel.TipoImovel;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ImovelRequest(
        @NotBlank
        String titulo,

        @NotBlank
        String descricao,

        @NotNull
        @DecimalMin("0.0")
        BigDecimal preco,

        @NotNull
        TipoImovel tipo,

        @NotBlank
        String cidade,

        @NotBlank
        String bairro,

        String endereco,

        @Min(0)
        Integer quartos,

        @Min(0)
        Integer banheiros,

        @Min(0)
        Integer vagas,

        @DecimalMin("0.0")
        BigDecimal area,

        StatusImovel status
) {
}
