package com.lorenzo.imobiliaria_api.imovel.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ImagemImovelOrdemRequest(
        @NotEmpty
        List<@NotNull Long> imagemIds
) {
}
