package com.lorenzo.imobiliaria_api.imovel.dto;

import com.lorenzo.imobiliaria_api.imovel.ImagemImovel;

public record ImagemImovelResponse(
        Long id,
        String url,
        Integer ordem,
        Boolean capa
) {

    public static ImagemImovelResponse fromEntity(ImagemImovel imagem) {
        return new ImagemImovelResponse(
                imagem.getId(),
                imagem.getUrl(),
                imagem.getOrdem(),
                imagem.getCapa()
        );
    }
}
