package com.lorenzo.imobiliaria_api.imovel.service;

import com.lorenzo.imobiliaria_api.imovel.Imovel;
import com.lorenzo.imobiliaria_api.imovel.ImovelRepository;
import com.lorenzo.imobiliaria_api.imovel.ImagemImovelRepository;
import com.lorenzo.imobiliaria_api.imovel.StatusImovel;
import com.lorenzo.imobiliaria_api.imovel.TipoImovel;
import com.lorenzo.imobiliaria_api.imovel.dto.ImovelFiltroRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ImovelServiceTest {

    private ImovelRepository imovelRepository;
    private ImovelService imovelService;

    @BeforeEach
    void setUp() {
        imovelRepository = mock(ImovelRepository.class);
        ImagemImovelRepository imagemImovelRepository = mock(ImagemImovelRepository.class);
        imovelService = new ImovelService(imovelRepository, imagemImovelRepository);
    }

    @Test
    void deveConsultarPublicadosComParametrosValidos() {
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
        )).thenReturn(new PageImpl<>(List.of(imovel())));

        imovelService.listarPublicados(
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

    private ImovelFiltroRequest filtroValido() {
        return new ImovelFiltroRequest(null, null, null, null, null, null, null, null, null);
    }

    private Imovel imovel() {
        Imovel imovel = new Imovel();
        imovel.setTitulo("Casa terrea com quintal amplo");
        imovel.setDescricao("Casa bem iluminada, com ambientes integrados.");
        imovel.setPreco(new BigDecimal("650000.00"));
        imovel.setTipo(TipoImovel.CASA);
        imovel.setCidade("Presidente Prudente");
        imovel.setBairro("Centro");
        imovel.setStatus(StatusImovel.PUBLICADO);

        return imovel;
    }

    private void assertBadRequest(Runnable runnable, String mensagem) {
        assertThatThrownBy(runnable::run)
                .isInstanceOfSatisfying(ResponseStatusException.class, exception -> {
                    assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(exception.getReason()).isEqualTo(mensagem);
                });
    }
}
