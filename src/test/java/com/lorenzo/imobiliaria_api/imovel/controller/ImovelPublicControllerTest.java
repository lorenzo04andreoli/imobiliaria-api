package com.lorenzo.imobiliaria_api.imovel.controller;

import com.lorenzo.imobiliaria_api.imovel.StatusImovel;
import com.lorenzo.imobiliaria_api.imovel.TipoImovel;
import com.lorenzo.imobiliaria_api.imovel.dto.ImovelFiltroRequest;
import com.lorenzo.imobiliaria_api.imovel.dto.ImovelResponse;
import com.lorenzo.imobiliaria_api.imovel.dto.PaginaResponse;
import com.lorenzo.imobiliaria_api.imovel.service.ImovelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ImovelPublicControllerTest {

    private ImovelService imovelService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        imovelService = mock(ImovelService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new ImovelPublicController(imovelService)).build();
    }

    @Test
    void deveListarImoveisPublicosComFiltrosPaginacaoEOrdenacao() throws Exception {
        ImovelResponse imovel = imovelResponse();
        PaginaResponse<ImovelResponse> response = new PaginaResponse<>(
                List.of(imovel),
                1,
                6,
                13,
                3,
                false,
                false
        );

        when(imovelService.listarPublicados(
                any(ImovelFiltroRequest.class),
                eq(1),
                eq(6),
                eq("preco"),
                eq("asc")
        )).thenReturn(response);

        mockMvc.perform(get("/api/imoveis")
                        .param("q", "quintal")
                        .param("cidade", "Presidente Prudente")
                        .param("bairro", "Centro")
                        .param("tipo", "CASA")
                        .param("precoMin", "300000")
                        .param("precoMax", "700000")
                        .param("quartosMin", "2")
                        .param("banheirosMin", "1")
                        .param("vagasMin", "1")
                        .param("page", "1")
                        .param("size", "6")
                        .param("sort", "preco")
                        .param("direction", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(10))
                .andExpect(jsonPath("$.content[0].titulo").value("Casa terrea com quintal amplo"))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(6))
                .andExpect(jsonPath("$.totalElements").value(13))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.first").value(false))
                .andExpect(jsonPath("$.last").value(false));

        ArgumentCaptor<ImovelFiltroRequest> filtroCaptor = ArgumentCaptor.forClass(ImovelFiltroRequest.class);
        verify(imovelService).listarPublicados(filtroCaptor.capture(), eq(1), eq(6), eq("preco"), eq("asc"));

        ImovelFiltroRequest filtro = filtroCaptor.getValue();
        assertThat(filtro.q()).isEqualTo("quintal");
        assertThat(filtro.cidade()).isEqualTo("Presidente Prudente");
        assertThat(filtro.bairro()).isEqualTo("Centro");
        assertThat(filtro.tipo()).isEqualTo(TipoImovel.CASA);
        assertThat(filtro.precoMin()).isEqualByComparingTo("300000");
        assertThat(filtro.precoMax()).isEqualByComparingTo("700000");
        assertThat(filtro.quartosMin()).isEqualTo(2);
        assertThat(filtro.banheirosMin()).isEqualTo(1);
        assertThat(filtro.vagasMin()).isEqualTo(1);
    }

    @Test
    void deveBuscarImovelPublicadoPorId() throws Exception {
        when(imovelService.buscarPublicadoPorId(10L)).thenReturn(imovelResponse());

        mockMvc.perform(get("/api/imoveis/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.titulo").value("Casa terrea com quintal amplo"));

        verify(imovelService).buscarPublicadoPorId(10L);
    }

    private ImovelResponse imovelResponse() {
        return new ImovelResponse(
                10L,
                "Casa terrea com quintal amplo",
                "Casa bem iluminada, com ambientes integrados.",
                new BigDecimal("650000.00"),
                TipoImovel.CASA,
                "Presidente Prudente",
                "Centro",
                "Rua das Palmeiras, 120",
                3,
                2,
                2,
                new BigDecimal("180.00"),
                StatusImovel.PUBLICADO,
                List.of(),
                LocalDateTime.of(2026, 1, 1, 10, 0),
                LocalDateTime.of(2026, 1, 1, 10, 0)
        );
    }
}
