package com.lorenzo.imobiliaria_api.imovel.controller;

import com.lorenzo.imobiliaria_api.imovel.dto.ImovelResponse;
import com.lorenzo.imobiliaria_api.imovel.service.ImovelService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/imoveis")
@RequiredArgsConstructor
public class ImovelPublicController {

    private final ImovelService imovelService;

    @GetMapping
    public List<ImovelResponse> listar() {
        return imovelService.listarPublicados();
    }

    @GetMapping("/{id}")
    public ImovelResponse buscarPorId(@PathVariable Long id) {
        return imovelService.buscarPublicadoPorId(id);
    }
}
