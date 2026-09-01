package com.lorenzo.imobiliaria_api.imovel.controller;

import com.lorenzo.imobiliaria_api.imovel.dto.ImovelRequest;
import com.lorenzo.imobiliaria_api.imovel.dto.ImovelResponse;
import com.lorenzo.imobiliaria_api.imovel.service.ImovelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ImovelResponse criar(@RequestBody @Valid ImovelRequest request) {
        return imovelService.criar(request);
    }

    @PutMapping("/{id}")
    public ImovelResponse atualizar(@PathVariable Long id, @RequestBody @Valid ImovelRequest request) {
        return imovelService.atualizar(id, request);
    }

    @PatchMapping("/{id}/inativar")
    public ImovelResponse inativar(@PathVariable Long id) {
        return imovelService.inativar(id);
    }
}
