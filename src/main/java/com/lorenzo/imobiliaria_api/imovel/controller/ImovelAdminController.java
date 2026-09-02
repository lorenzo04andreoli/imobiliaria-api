package com.lorenzo.imobiliaria_api.imovel.controller;

import com.lorenzo.imobiliaria_api.imovel.dto.ImagemImovelRequest;
import com.lorenzo.imobiliaria_api.imovel.dto.ImagemImovelResponse;
import com.lorenzo.imobiliaria_api.imovel.dto.ImovelRequest;
import com.lorenzo.imobiliaria_api.imovel.dto.ImovelResponse;
import com.lorenzo.imobiliaria_api.imovel.service.ImovelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin/imoveis")
@RequiredArgsConstructor
public class ImovelAdminController {

    private final ImovelService imovelService;

    @GetMapping
    public List<ImovelResponse> listar() {
        return imovelService.listarTodos();
    }

    @GetMapping("/{id}")
    public ImovelResponse buscarPorId(@PathVariable Long id) {
        return imovelService.buscarPorIdAdmin(id);
    }

    @GetMapping("/{id}/imagens")
    public List<ImagemImovelResponse> listarImagens(@PathVariable Long id) {
        return imovelService.listarImagens(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ImovelResponse criar(@RequestBody @Valid ImovelRequest request) {
        return imovelService.criar(request);
    }

    @PostMapping("/{id}/imagens")
    @ResponseStatus(HttpStatus.CREATED)
    public ImagemImovelResponse adicionarImagem(
            @PathVariable Long id,
            @RequestBody @Valid ImagemImovelRequest request
    ) {
        return imovelService.adicionarImagem(id, request);
    }

    @PostMapping(value = "/{id}/imagens/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ImagemImovelResponse uploadImagem(
            @PathVariable Long id,
            @RequestParam("arquivo") MultipartFile arquivo,
            @RequestParam(required = false) Integer ordem,
            @RequestParam(required = false) Boolean capa
    ) {
        return imovelService.uploadImagem(id, arquivo, ordem, capa);
    }

    @PatchMapping("/{id}/imagens/{imagemId}/capa")
    public ImagemImovelResponse definirImagemComoCapa(
            @PathVariable Long id,
            @PathVariable Long imagemId
    ) {
        return imovelService.definirImagemComoCapa(id, imagemId);
    }

    @DeleteMapping("/{id}/imagens/{imagemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removerImagem(
            @PathVariable Long id,
            @PathVariable Long imagemId
    ) {
        imovelService.removerImagem(id, imagemId);
    }

    @PutMapping("/{id}")
    public ImovelResponse atualizar(@PathVariable Long id, @RequestBody @Valid ImovelRequest request) {
        return imovelService.atualizar(id, request);
    }

    @PatchMapping("/{id}/inativar")
    public ImovelResponse inativar(@PathVariable Long id) {
        return imovelService.inativar(id);
    }

    @PatchMapping("/{id}/publicar")
    public ImovelResponse publicar(@PathVariable Long id) {
        return imovelService.publicar(id);
    }

    @PatchMapping("/{id}/vender")
    public ImovelResponse vender(@PathVariable Long id) {
        return imovelService.marcarComoVendido(id);
    }

    @PatchMapping("/{id}/rascunho")
    public ImovelResponse rascunho(@PathVariable Long id) {
        return imovelService.marcarComoRascunho(id);
    }
}
