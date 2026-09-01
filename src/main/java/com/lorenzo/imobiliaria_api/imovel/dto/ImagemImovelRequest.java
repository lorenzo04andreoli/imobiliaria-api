package com.lorenzo.imobiliaria_api.imovel.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ImagemImovelRequest(
        @NotBlank
        String url,

        @Min(0)
        Integer ordem,

        Boolean capa
) {
}
