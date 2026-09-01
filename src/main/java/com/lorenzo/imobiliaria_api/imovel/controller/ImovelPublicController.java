package com.lorenzo.imobiliaria_api.imovel.controller;

import com.lorenzo.imobiliaria_api.imovel.TipoImovel;
import com.lorenzo.imobiliaria_api.imovel.dto.ImovelFiltroRequest;
import com.lorenzo.imobiliaria_api.imovel.dto.ImovelResponse;
import com.lorenzo.imobiliaria_api.imovel.dto.PaginaResponse;
import com.lorenzo.imobiliaria_api.imovel.service.ImovelService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/imoveis")
@RequiredArgsConstructor
public class ImovelPublicController {

    private final ImovelService imovelService;

    @GetMapping
    public PaginaResponse<ImovelResponse> listar(
            @RequestParam(required = false) String cidade,
            @RequestParam(required = false) String bairro,
            @RequestParam(required = false) TipoImovel tipo,
            @RequestParam(required = false) BigDecimal precoMin,
            @RequestParam(required = false) BigDecimal precoMax,
            @RequestParam(required = false) Integer quartosMin,
            @RequestParam(required = false) Integer banheirosMin,
            @RequestParam(required = false) Integer vagasMin,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        ImovelFiltroRequest filtro = new ImovelFiltroRequest(
                cidade,
                bairro,
                tipo,
                precoMin,
                precoMax,
                quartosMin,
                banheirosMin,
                vagasMin
        );

        return imovelService.listarPublicados(filtro, page, size);
    }

    @GetMapping("/{id}")
    public ImovelResponse buscarPorId(@PathVariable Long id) {
        return imovelService.buscarPublicadoPorId(id);
    }
}
