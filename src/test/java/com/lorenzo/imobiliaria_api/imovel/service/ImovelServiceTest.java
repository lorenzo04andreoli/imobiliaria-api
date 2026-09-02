package com.lorenzo.imobiliaria_api.imovel.service;

import com.lorenzo.imobiliaria_api.imovel.Imovel;
import com.lorenzo.imobiliaria_api.imovel.ImovelRepository;
import com.lorenzo.imobiliaria_api.imovel.ImagemImovel;
import com.lorenzo.imobiliaria_api.imovel.ImagemImovelRepository;
import com.lorenzo.imobiliaria_api.imovel.StatusImovel;
import com.lorenzo.imobiliaria_api.imovel.TipoImovel;
import com.lorenzo.imobiliaria_api.imovel.dto.ImagemImovelOrdemRequest;
import com.lorenzo.imobiliaria_api.imovel.dto.ImovelFiltroRequest;
import com.lorenzo.imobiliaria_api.imovel.dto.PaginaResponse;
import com.lorenzo.imobiliaria_api.imovel.dto.ImovelResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ImovelServiceTest {

    private ImovelRepository imovelRepository;
    private ImagemImovelRepository imagemImovelRepository;
    private ImovelService imovelService;
    private Path uploadDir;

    @BeforeEach
    void setUp() {
        imovelRepository = mock(ImovelRepository.class);
        imagemImovelRepository = mock(ImagemImovelRepository.class);
        imovelService = new ImovelService(imovelRepository, imagemImovelRepository);
    }

    @Test
    void deveConsultarPublicadosComParametrosValidos() {
        Imovel imovel = imovel();
        ImagemImovel imagem = imagem(imovel);

        when(imovelRepository.buscarPublicadosComFiltros(
                eq(StatusImovel.PUBLICADO),
                eq("quintal"),
                eq("Presidente Prudente"),
                eq("Centro"),
                eq(TipoImovel.CASA),
                eq(new BigDecimal("300000")),
                eq(new BigDecimal("700000")),
                eq(2),
                eq(1),
                eq(1),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of(imovel)));
        when(imagemImovelRepository.findByImovelIdInOrderByImovelIdAscOrdemAsc(List.of(10L)))
                .thenReturn(List.of(imagem));

        PaginaResponse<ImovelResponse> response = imovelService.listarPublicados(
                new ImovelFiltroRequest(
                        " quintal ",
                        " Presidente Prudente ",
                        " Centro ",
                        TipoImovel.CASA,
                        new BigDecimal("300000"),
                        new BigDecimal("700000"),
                        2,
                        1,
                        1
                ),
                0,
                12,
                "preco",
                "asc"
        );

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(imovelRepository).buscarPublicadosComFiltros(
                eq(StatusImovel.PUBLICADO),
                eq("quintal"),
                eq("Presidente Prudente"),
                eq("Centro"),
                eq(TipoImovel.CASA),
                eq(new BigDecimal("300000")),
                eq(new BigDecimal("700000")),
                eq(2),
                eq(1),
                eq(1),
                pageableCaptor.capture()
        );

        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageNumber()).isZero();
        assertThat(pageable.getPageSize()).isEqualTo(12);
        assertThat(pageable.getSort().getOrderFor("preco")).isNotNull();
        assertThat(pageable.getSort().getOrderFor("preco").isAscending()).isTrue();
        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).imagens()).hasSize(1);
        assertThat(response.content().get(0).imagens().get(0).url()).isEqualTo("https://example.com/imovel.jpg");
        verify(imagemImovelRepository).findByImovelIdInOrderByImovelIdAscOrdemAsc(List.of(10L));
    }

    @Test
    void deveValidarPaginaNegativa() {
        assertBadRequest(
                () -> imovelService.listarPublicados(filtroValido(), -1, 12, "criadoEm", "desc"),
                "O parametro page deve ser maior ou igual a 0"
        );
    }

    @Test
    void deveValidarTamanhoDaPagina() {
        assertBadRequest(
                () -> imovelService.listarPublicados(filtroValido(), 0, 51, "criadoEm", "desc"),
                "O parametro size deve estar entre 1 e 50"
        );
    }

    @Test
    void deveValidarCampoDeOrdenacao() {
        assertBadRequest(
                () -> imovelService.listarPublicados(filtroValido(), 0, 12, "titulo", "desc"),
                "O parametro sort deve ser um campo permitido"
        );
    }

    @Test
    void deveValidarDirecaoDeOrdenacao() {
        assertBadRequest(
                () -> imovelService.listarPublicados(filtroValido(), 0, 12, "preco", "maior"),
                "O parametro direction deve ser asc ou desc"
        );
    }

    @Test
    void deveValidarFaixaDePreco() {
        ImovelFiltroRequest filtro = new ImovelFiltroRequest(
                null,
                null,
                null,
                null,
                new BigDecimal("700000"),
                new BigDecimal("300000"),
                null,
                null,
                null
        );

        assertBadRequest(
                () -> imovelService.listarPublicados(filtro, 0, 12, "preco", "asc"),
                "O parametro precoMin deve ser menor ou igual a precoMax"
        );
    }

    @Test
    void deveFazerUploadDeImagemDoImovel(@org.junit.jupiter.api.io.TempDir Path tempDir) throws IOException {
        uploadDir = tempDir.resolve("imoveis");
        Imovel imovel = imovel();

        when(imovelRepository.findById(10L)).thenReturn(Optional.of(imovel));
        when(imagemImovelRepository.countByImovelId(10L)).thenReturn(0L);
        when(imagemImovelRepository.findByImovelIdOrderByOrdemAsc(10L)).thenReturn(List.of());
        when(imagemImovelRepository.save(any(ImagemImovel.class)))
                .thenAnswer(invocation -> {
                    ImagemImovel imagem = invocation.getArgument(0);
                    imagem.setId(7L);
                    return imagem;
                });

        ReflectionTestUtils.setField(imovelService, "uploadImoveisDir", uploadDir.toString());
        ReflectionTestUtils.setField(imovelService, "uploadPublicPath", "/uploads/imoveis");

        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo",
                "fachada.png",
                "image/png",
                "conteudo".getBytes()
        );

        var response = imovelService.uploadImagem(10L, arquivo, 2, true);

        assertThat(response.id()).isEqualTo(7L);
        assertThat(response.url()).startsWith("/uploads/imoveis/");
        assertThat(response.url()).endsWith(".png");
        assertThat(response.ordem()).isEqualTo(2);
        assertThat(response.capa()).isTrue();
        try (var arquivos = Files.list(uploadDir)) {
            assertThat(arquivos).hasSize(1);
        }
        verify(imovelRepository).findById(10L);
        verify(imagemImovelRepository).save(any(ImagemImovel.class));
    }

    @Test
    void deveValidarTipoDeArquivoNoUpload() {
        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo",
                "arquivo.txt",
                "text/plain",
                "conteudo".getBytes()
        );

        assertBadRequest(
                () -> imovelService.uploadImagem(10L, arquivo, null, null),
                "Arquivo deve ser uma imagem JPG, PNG ou WEBP"
        );

        verifyNoInteractions(imagemImovelRepository);
    }

    @Test
    void deveRemoverImagemLocalEPromoverNovaCapa(@org.junit.jupiter.api.io.TempDir Path tempDir) throws IOException {
        uploadDir = tempDir.resolve("imoveis");
        Files.createDirectories(uploadDir);
        Path arquivo = uploadDir.resolve("fachada.png");
        Files.writeString(arquivo, "conteudo");

        Imovel imovel = imovel();
        ImagemImovel imagemRemovida = imagem(imovel);
        imagemRemovida.setUrl("/uploads/imoveis/fachada.png");
        imagemRemovida.setCapa(true);

        ImagemImovel novaCapa = imagem(imovel);
        novaCapa.setId(8L);
        novaCapa.setUrl("https://example.com/sala.jpg");
        novaCapa.setCapa(false);

        when(imovelRepository.findById(10L)).thenReturn(Optional.of(imovel));
        when(imagemImovelRepository.findByIdAndImovelId(7L, 10L)).thenReturn(Optional.of(imagemRemovida));
        when(imagemImovelRepository.findByImovelIdOrderByOrdemAsc(10L))
                .thenReturn(List.of(imagemRemovida, novaCapa));

        ReflectionTestUtils.setField(imovelService, "uploadImoveisDir", uploadDir.toString());
        ReflectionTestUtils.setField(imovelService, "uploadPublicPath", "/uploads/imoveis");

        imovelService.removerImagem(10L, 7L);

        assertThat(Files.exists(arquivo)).isFalse();
        assertThat(novaCapa.getCapa()).isTrue();
        verify(imovelRepository).findById(10L);
        verify(imagemImovelRepository).delete(imagemRemovida);
        verify(imagemImovelRepository).save(novaCapa);
    }

    @Test
    void deveReordenarImagensDoImovel() {
        Imovel imovel = imovel();
        ImagemImovel primeira = imagem(imovel);
        primeira.setId(7L);
        primeira.setOrdem(0);

        ImagemImovel segunda = imagem(imovel);
        segunda.setId(8L);
        segunda.setUrl("https://example.com/sala.jpg");
        segunda.setOrdem(1);

        when(imovelRepository.findById(10L)).thenReturn(Optional.of(imovel));
        when(imagemImovelRepository.findByImovelIdOrderByOrdemAsc(10L))
                .thenReturn(List.of(primeira, segunda));
        when(imagemImovelRepository.saveAll(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = imovelService.reordenarImagens(
                10L,
                new ImagemImovelOrdemRequest(List.of(8L, 7L))
        );

        assertThat(response).hasSize(2);
        assertThat(response.get(0).id()).isEqualTo(8L);
        assertThat(response.get(0).ordem()).isZero();
        assertThat(response.get(1).id()).isEqualTo(7L);
        assertThat(response.get(1).ordem()).isEqualTo(1);
        assertThat(segunda.getOrdem()).isZero();
        assertThat(primeira.getOrdem()).isEqualTo(1);
        verify(imagemImovelRepository).saveAll(List.of(primeira, segunda));
    }

    @Test
    void deveValidarIdsRepetidosNaReordenacao() {
        when(imovelRepository.findById(10L)).thenReturn(Optional.of(imovel()));

        assertBadRequest(
                () -> imovelService.reordenarImagens(10L, new ImagemImovelOrdemRequest(List.of(7L, 7L))),
                "A lista de imagens nao pode conter IDs repetidos"
        );
    }

    private ImovelFiltroRequest filtroValido() {
        return new ImovelFiltroRequest(null, null, null, null, null, null, null, null, null);
    }

    private Imovel imovel() {
        Imovel imovel = new Imovel();
        imovel.setId(10L);
        imovel.setTitulo("Casa terrea com quintal amplo");
        imovel.setDescricao("Casa bem iluminada, com ambientes integrados.");
        imovel.setPreco(new BigDecimal("650000.00"));
        imovel.setTipo(TipoImovel.CASA);
        imovel.setCidade("Presidente Prudente");
        imovel.setBairro("Centro");
        imovel.setStatus(StatusImovel.PUBLICADO);

        return imovel;
    }

    private ImagemImovel imagem(Imovel imovel) {
        ImagemImovel imagem = new ImagemImovel();
        imagem.setId(7L);
        imagem.setImovel(imovel);
        imagem.setUrl("https://example.com/imovel.jpg");
        imagem.setOrdem(0);
        imagem.setCapa(true);

        return imagem;
    }

    private void assertBadRequest(Runnable runnable, String mensagem) {
        assertThatThrownBy(runnable::run)
                .isInstanceOfSatisfying(ResponseStatusException.class, exception -> {
                    assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(exception.getReason()).isEqualTo(mensagem);
                });
    }
}
