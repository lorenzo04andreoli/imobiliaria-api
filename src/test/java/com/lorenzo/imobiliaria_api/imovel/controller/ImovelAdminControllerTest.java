package com.lorenzo.imobiliaria_api.imovel.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lorenzo.imobiliaria_api.imovel.StatusImovel;
import com.lorenzo.imobiliaria_api.imovel.TipoImovel;
import com.lorenzo.imobiliaria_api.imovel.dto.ImagemImovelOrdemRequest;
import com.lorenzo.imobiliaria_api.imovel.dto.ImagemImovelRequest;
import com.lorenzo.imobiliaria_api.imovel.dto.ImagemImovelResponse;
import com.lorenzo.imobiliaria_api.imovel.dto.ImovelFiltroRequest;
import com.lorenzo.imobiliaria_api.imovel.dto.ImovelRequest;
import com.lorenzo.imobiliaria_api.imovel.dto.ImovelResponse;
import com.lorenzo.imobiliaria_api.imovel.dto.PaginaResponse;
import com.lorenzo.imobiliaria_api.imovel.service.ImovelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ImovelAdminControllerTest {

    private ImovelService imovelService;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        imovelService = mock(ImovelService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new ImovelAdminController(imovelService)).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void deveListarEBuscarImoveisAdministrativos() throws Exception {
        when(imovelService.listarTodos(
                any(ImovelFiltroRequest.class),
                eq(StatusImovel.RASCUNHO),
                eq(0),
                eq(12),
                eq("criadoEm"),
                eq("desc")
        )).thenReturn(new PaginaResponse<>(
                List.of(imovelResponse(StatusImovel.RASCUNHO)),
                0,
                12,
                1,
                1,
                true,
                true
        ));
        when(imovelService.buscarPorIdAdmin(10L)).thenReturn(imovelResponse(StatusImovel.RASCUNHO));

        mockMvc.perform(get("/api/admin/imoveis")
                        .param("q", "quintal")
                        .param("cidade", "Presidente Prudente")
                        .param("bairro", "Centro")
                        .param("tipo", "CASA")
                        .param("status", "RASCUNHO")
                        .param("precoMin", "300000")
                        .param("precoMax", "700000")
                        .param("quartosMin", "2")
                        .param("banheirosMin", "1")
                        .param("vagasMin", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(10))
                .andExpect(jsonPath("$.content[0].status").value("RASCUNHO"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(12))
                .andExpect(jsonPath("$.totalElements").value(1));

        mockMvc.perform(get("/api/admin/imoveis/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("RASCUNHO"));

        ArgumentCaptor<ImovelFiltroRequest> filtroCaptor = ArgumentCaptor.forClass(ImovelFiltroRequest.class);
        verify(imovelService).listarTodos(
                filtroCaptor.capture(),
                eq(StatusImovel.RASCUNHO),
                eq(0),
                eq(12),
                eq("criadoEm"),
                eq("desc")
        );
        assertThat(filtroCaptor.getValue().q()).isEqualTo("quintal");
        assertThat(filtroCaptor.getValue().tipo()).isEqualTo(TipoImovel.CASA);
        assertThat(filtroCaptor.getValue().precoMin()).isEqualByComparingTo("300000");
        verify(imovelService).buscarPorIdAdmin(10L);
    }

    @Test
    void deveCriarEAtualizarImovel() throws Exception {
        when(imovelService.criar(any(ImovelRequest.class))).thenReturn(imovelResponse(StatusImovel.RASCUNHO));
        when(imovelService.atualizar(eq(10L), any(ImovelRequest.class)))
                .thenReturn(imovelResponse(StatusImovel.PUBLICADO));

        ImovelRequest request = imovelRequest(StatusImovel.RASCUNHO);

        mockMvc.perform(post("/api/admin/imoveis")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("RASCUNHO"));

        mockMvc.perform(put("/api/admin/imoveis/10")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(imovelRequest(StatusImovel.PUBLICADO))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("PUBLICADO"));

        ArgumentCaptor<ImovelRequest> criarCaptor = ArgumentCaptor.forClass(ImovelRequest.class);
        verify(imovelService).criar(criarCaptor.capture());
        assertThat(criarCaptor.getValue().titulo()).isEqualTo("Casa terrea com quintal amplo");

        verify(imovelService).atualizar(eq(10L), any(ImovelRequest.class));
    }

    @Test
    void deveAlterarStatusDoImovel() throws Exception {
        when(imovelService.publicar(10L)).thenReturn(imovelResponse(StatusImovel.PUBLICADO));
        when(imovelService.marcarComoVendido(10L)).thenReturn(imovelResponse(StatusImovel.VENDIDO));
        when(imovelService.marcarComoRascunho(10L)).thenReturn(imovelResponse(StatusImovel.RASCUNHO));
        when(imovelService.inativar(10L)).thenReturn(imovelResponse(StatusImovel.INATIVO));

        mockMvc.perform(patch("/api/admin/imoveis/10/publicar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLICADO"));

        mockMvc.perform(patch("/api/admin/imoveis/10/vender"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("VENDIDO"));

        mockMvc.perform(patch("/api/admin/imoveis/10/rascunho"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RASCUNHO"));

        mockMvc.perform(patch("/api/admin/imoveis/10/inativar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INATIVO"));

        verify(imovelService).publicar(10L);
        verify(imovelService).marcarComoVendido(10L);
        verify(imovelService).marcarComoRascunho(10L);
        verify(imovelService).inativar(10L);
    }

    @Test
    void deveGerenciarImagensDoImovel() throws Exception {
        ImagemImovelResponse imagem = new ImagemImovelResponse(
                7L,
                "https://example.com/imovel.jpg",
                0,
                true
        );

        when(imovelService.listarImagens(10L)).thenReturn(List.of(imagem));
        when(imovelService.adicionarImagem(eq(10L), any(ImagemImovelRequest.class)))
                .thenReturn(imagem);
        when(imovelService.definirImagemComoCapa(10L, 7L)).thenReturn(imagem);

        mockMvc.perform(get("/api/admin/imoveis/10/imagens"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(7))
                .andExpect(jsonPath("$[0].capa").value(true));

        mockMvc.perform(post("/api/admin/imoveis/10/imagens")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ImagemImovelRequest(
                                "https://example.com/imovel.jpg",
                                0,
                                true
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.capa").value(true));

        mockMvc.perform(patch("/api/admin/imoveis/10/imagens/7/capa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.capa").value(true));

        verify(imovelService).listarImagens(10L);
        verify(imovelService).adicionarImagem(eq(10L), any(ImagemImovelRequest.class));
        verify(imovelService).definirImagemComoCapa(10L, 7L);
    }

    @Test
    void deveReordenarImagensDoImovel() throws Exception {
        ImagemImovelResponse primeira = new ImagemImovelResponse(
                8L,
                "https://example.com/sala.jpg",
                0,
                false
        );
        ImagemImovelResponse segunda = new ImagemImovelResponse(
                7L,
                "https://example.com/imovel.jpg",
                1,
                true
        );

        when(imovelService.reordenarImagens(eq(10L), any(ImagemImovelOrdemRequest.class)))
                .thenReturn(List.of(primeira, segunda));

        mockMvc.perform(put("/api/admin/imoveis/10/imagens/ordem")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ImagemImovelOrdemRequest(List.of(8L, 7L)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(8))
                .andExpect(jsonPath("$[0].ordem").value(0))
                .andExpect(jsonPath("$[1].id").value(7))
                .andExpect(jsonPath("$[1].ordem").value(1));

        verify(imovelService).reordenarImagens(eq(10L), any(ImagemImovelOrdemRequest.class));
    }

    @Test
    void deveRemoverImagemDoImovel() throws Exception {
        mockMvc.perform(delete("/api/admin/imoveis/10/imagens/7"))
                .andExpect(status().isNoContent());

        verify(imovelService).removerImagem(10L, 7L);
    }

    @Test
    void deveFazerUploadDeImagemDoImovel() throws Exception {
        ImagemImovelResponse imagem = new ImagemImovelResponse(
                7L,
                "/uploads/imoveis/foto.jpg",
                0,
                true
        );

        when(imovelService.uploadImagem(eq(10L), any(MultipartFile.class), eq(0), eq(true)))
                .thenReturn(imagem);

        mockMvc.perform(multipart("/api/admin/imoveis/10/imagens/upload")
                        .file("arquivo", "conteudo".getBytes())
                        .param("ordem", "0")
                        .param("capa", "true"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.url").value("/uploads/imoveis/foto.jpg"))
                .andExpect(jsonPath("$.capa").value(true));

        verify(imovelService).uploadImagem(eq(10L), any(MultipartFile.class), eq(0), eq(true));
    }

    private ImovelRequest imovelRequest(StatusImovel status) {
        return new ImovelRequest(
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
                status
        );
    }

    private ImovelResponse imovelResponse(StatusImovel status) {
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
                status,
                List.of(),
                LocalDateTime.of(2026, 1, 1, 10, 0),
                LocalDateTime.of(2026, 1, 1, 10, 0)
        );
    }
}
