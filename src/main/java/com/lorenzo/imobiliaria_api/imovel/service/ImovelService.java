package com.lorenzo.imobiliaria_api.imovel.service;

import com.lorenzo.imobiliaria_api.imovel.ImovelRepository;
import com.lorenzo.imobiliaria_api.imovel.StatusImovel;
import com.lorenzo.imobiliaria_api.imovel.dto.ImovelResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ImovelService {

    private final ImovelRepository imovelRepository;

    public List<ImovelResponse> listarPublicados() {
        return imovelRepository.findByStatusOrderByCriadoEmDesc(StatusImovel.PUBLICADO)
                .stream()
                .map(ImovelResponse::fromEntity)
                .toList();
    }

    public ImovelResponse buscarPublicadoPorId(Long id) {
        return imovelRepository.findByIdAndStatus(id, StatusImovel.PUBLICADO)
                .map(ImovelResponse::fromEntity)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Imovel nao encontrado"));
    }
}
