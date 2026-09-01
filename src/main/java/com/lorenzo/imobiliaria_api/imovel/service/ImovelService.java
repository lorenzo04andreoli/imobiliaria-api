package com.lorenzo.imobiliaria_api.imovel.service;

import com.lorenzo.imobiliaria_api.imovel.Imovel;
import com.lorenzo.imobiliaria_api.imovel.ImovelRepository;
import com.lorenzo.imobiliaria_api.imovel.StatusImovel;
import com.lorenzo.imobiliaria_api.imovel.dto.ImovelRequest;
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

    public List<ImovelResponse> listarTodos() {
        return imovelRepository.findAllByOrderByCriadoEmDesc()
                .stream()
                .map(ImovelResponse::fromEntity)
                .toList();
    }

    public ImovelResponse buscarPorIdAdmin(Long id) {
        return ImovelResponse.fromEntity(buscarPorId(id));
    }

    public ImovelResponse criar(ImovelRequest request) {
        Imovel imovel = new Imovel();
        preencherDados(imovel, request);

        return ImovelResponse.fromEntity(imovelRepository.save(imovel));
    }

    public ImovelResponse atualizar(Long id, ImovelRequest request) {
        Imovel imovel = buscarPorId(id);
        preencherDados(imovel, request);

        return ImovelResponse.fromEntity(imovelRepository.save(imovel));
    }

    public ImovelResponse inativar(Long id) {
        Imovel imovel = buscarPorId(id);
        imovel.setStatus(StatusImovel.INATIVO);

        return ImovelResponse.fromEntity(imovelRepository.save(imovel));
    }

    private Imovel buscarPorId(Long id) {
        return imovelRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Imovel nao encontrado"));
    }

    private void preencherDados(Imovel imovel, ImovelRequest request) {
        imovel.setTitulo(request.titulo());
        imovel.setDescricao(request.descricao());
        imovel.setPreco(request.preco());
        imovel.setTipo(request.tipo());
        imovel.setCidade(request.cidade());
        imovel.setBairro(request.bairro());
        imovel.setEndereco(request.endereco());
        imovel.setQuartos(request.quartos());
        imovel.setBanheiros(request.banheiros());
        imovel.setVagas(request.vagas());
        imovel.setArea(request.area());
        imovel.setStatus(request.status() != null ? request.status() : StatusImovel.RASCUNHO);
    }
}
