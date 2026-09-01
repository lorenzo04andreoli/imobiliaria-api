package com.lorenzo.imobiliaria_api.config;

import com.lorenzo.imobiliaria_api.imovel.ImagemImovel;
import com.lorenzo.imobiliaria_api.imovel.ImagemImovelRepository;
import com.lorenzo.imobiliaria_api.imovel.Imovel;
import com.lorenzo.imobiliaria_api.imovel.ImovelRepository;
import com.lorenzo.imobiliaria_api.imovel.StatusImovel;
import com.lorenzo.imobiliaria_api.imovel.TipoImovel;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class SampleDataInitializer implements CommandLineRunner {

    private final ImovelRepository imovelRepository;
    private final ImagemImovelRepository imagemImovelRepository;

    @Override
    public void run(String... args) {
        if (imovelRepository.count() > 0) {
            return;
        }

        criarImoveis();
    }

    private void criarImoveis() {
        criarImovel(
                "Casa terrea com quintal amplo",
                "Casa bem iluminada, com ambientes integrados e quintal para area gourmet.",
                "650000.00",
                TipoImovel.CASA,
                "Presidente Prudente",
                "Jardim Bongiovani",
                "Rua das Palmeiras, 120",
                3,
                2,
                2,
                "180.00",
                StatusImovel.PUBLICADO,
                List.of(
                        "https://images.unsplash.com/photo-1564013799919-ab600027ffc6",
                        "https://images.unsplash.com/photo-1600585154340-be6161a56a0c"
                )
        );

        criarImovel(
                "Apartamento proximo ao centro",
                "Apartamento em andar alto, com sacada e acesso rapido a mercados, escolas e servicos.",
                "420000.00",
                TipoImovel.APARTAMENTO,
                "Presidente Prudente",
                "Centro",
                "Avenida Brasil, 980",
                2,
                2,
                1,
                "74.00",
                StatusImovel.PUBLICADO,
                List.of(
                        "https://images.unsplash.com/photo-1522708323590-d24dbb6b0267",
                        "https://images.unsplash.com/photo-1505693416388-ac5ce068fe85"
                )
        );

        criarImovel(
                "Terreno plano em bairro tranquilo",
                "Terreno murado, pronto para construir, em rua residencial com boa valorizacao.",
                "210000.00",
                TipoImovel.TERRENO,
                "Presidente Prudente",
                "Residencial Florenza",
                "Rua dos Ipes, 45",
                0,
                0,
                0,
                "250.00",
                StatusImovel.PUBLICADO,
                List.of("https://images.unsplash.com/photo-1500382017468-9049fed747ef")
        );

        criarImovel(
                "Sobrado em preparacao para anuncio",
                "Imovel em revisao interna antes da publicacao no site.",
                "780000.00",
                TipoImovel.CASA,
                "Presidente Prudente",
                "Vila Formosa",
                "Endereco em revisao",
                4,
                3,
                2,
                "220.00",
                StatusImovel.RASCUNHO,
                List.of("https://images.unsplash.com/photo-1605276374104-dee2a0ed3cd6")
        );

        criarImovel(
                "Sala comercial vendida",
                "Sala comercial mantida no historico administrativo como imovel ja negociado.",
                "300000.00",
                TipoImovel.COMERCIAL,
                "Presidente Prudente",
                "Centro",
                "Rua Siqueira Campos, 210",
                0,
                1,
                1,
                "48.00",
                StatusImovel.VENDIDO,
                List.of("https://images.unsplash.com/photo-1497366754035-f200968a6e72")
        );
    }

    private void criarImovel(
            String titulo,
            String descricao,
            String preco,
            TipoImovel tipo,
            String cidade,
            String bairro,
            String endereco,
            Integer quartos,
            Integer banheiros,
            Integer vagas,
            String area,
            StatusImovel status,
            List<String> imagens
    ) {
        Imovel imovel = new Imovel();
        imovel.setTitulo(titulo);
        imovel.setDescricao(descricao);
        imovel.setPreco(new BigDecimal(preco));
        imovel.setTipo(tipo);
        imovel.setCidade(cidade);
        imovel.setBairro(bairro);
        imovel.setEndereco(endereco);
        imovel.setQuartos(quartos);
        imovel.setBanheiros(banheiros);
        imovel.setVagas(vagas);
        imovel.setArea(new BigDecimal(area));
        imovel.setStatus(status);

        Imovel imovelSalvo = imovelRepository.save(imovel);

        for (int indice = 0; indice < imagens.size(); indice++) {
            ImagemImovel imagem = new ImagemImovel();
            imagem.setImovel(imovelSalvo);
            imagem.setUrl(imagens.get(indice));
            imagem.setOrdem(indice);
            imagem.setCapa(indice == 0);

            imagemImovelRepository.save(imagem);
        }
    }
}
