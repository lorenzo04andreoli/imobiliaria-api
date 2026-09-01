package com.lorenzo.imobiliaria_api.imovel.service;

import com.lorenzo.imobiliaria_api.imovel.Imovel;
import com.lorenzo.imobiliaria_api.imovel.ImovelRepository;
import com.lorenzo.imobiliaria_api.imovel.ImagemImovel;
import com.lorenzo.imobiliaria_api.imovel.ImagemImovelRepository;
import com.lorenzo.imobiliaria_api.imovel.StatusImovel;
import com.lorenzo.imobiliaria_api.imovel.dto.ImagemImovelRequest;
import com.lorenzo.imobiliaria_api.imovel.dto.ImagemImovelResponse;
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
    private final ImagemImovelRepository imagemImovelRepository;

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

    public List<ImagemImovelResponse> listarImagens(Long imovelId) {
        buscarPorId(imovelId);

        return imagemImovelRepository.findByImovelIdOrderByOrdemAsc(imovelId)
                .stream()
                .map(ImagemImovelResponse::fromEntity)
                .toList();
    }

    public ImagemImovelResponse adicionarImagem(Long imovelId, ImagemImovelRequest request) {
        Imovel imovel = buscarPorId(imovelId);
        boolean primeiraImagem = imagemImovelRepository.countByImovelId(imovelId) == 0;

        ImagemImovel imagem = new ImagemImovel();
        imagem.setImovel(imovel);
        imagem.setUrl(request.url());
        imagem.setOrdem(request.ordem() != null ? request.ordem() : 0);
        imagem.setCapa(request.capa() != null ? request.capa() : primeiraImagem);

        if (Boolean.TRUE.equals(imagem.getCapa())) {
            removerCapaDasImagens(imovelId);
        }

        return ImagemImovelResponse.fromEntity(imagemImovelRepository.save(imagem));
    }

    public ImagemImovelResponse definirImagemComoCapa(Long imovelId, Long imagemId) {
        buscarPorId(imovelId);
        ImagemImovel imagem = buscarImagemDoImovel(imovelId, imagemId);

        removerCapaDasImagens(imovelId);
        imagem.setCapa(true);

        return ImagemImovelResponse.fromEntity(imagemImovelRepository.save(imagem));
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
        return alterarStatus(id, StatusImovel.INATIVO);
    }

    public ImovelResponse publicar(Long id) {
        return alterarStatus(id, StatusImovel.PUBLICADO);
    }

    public ImovelResponse marcarComoVendido(Long id) {
        return alterarStatus(id, StatusImovel.VENDIDO);
    }

    public ImovelResponse marcarComoRascunho(Long id) {
        return alterarStatus(id, StatusImovel.RASCUNHO);
    }

    private ImovelResponse alterarStatus(Long id, StatusImovel status) {
        Imovel imovel = buscarPorId(id);
        imovel.setStatus(status);

        return ImovelResponse.fromEntity(imovelRepository.save(imovel));
    }

    private Imovel buscarPorId(Long id) {
        return imovelRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Imovel nao encontrado"));
    }

    private ImagemImovel buscarImagemDoImovel(Long imovelId, Long imagemId) {
        return imagemImovelRepository.findByIdAndImovelId(imagemId, imovelId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Imagem do imovel nao encontrada"));
    }

    private void removerCapaDasImagens(Long imovelId) {
        List<ImagemImovel> imagens = imagemImovelRepository.findByImovelIdOrderByOrdemAsc(imovelId);
        imagens.forEach(imagem -> imagem.setCapa(false));
        imagemImovelRepository.saveAll(imagens);
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
