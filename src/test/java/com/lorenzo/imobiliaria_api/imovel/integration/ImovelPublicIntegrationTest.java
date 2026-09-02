package com.lorenzo.imobiliaria_api.imovel.integration;

import com.lorenzo.imobiliaria_api.imovel.ImagemImovel;
import com.lorenzo.imobiliaria_api.imovel.ImagemImovelRepository;
import com.lorenzo.imobiliaria_api.imovel.Imovel;
import com.lorenzo.imobiliaria_api.imovel.ImovelRepository;
import com.lorenzo.imobiliaria_api.imovel.StatusImovel;
import com.lorenzo.imobiliaria_api.imovel.TipoImovel;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest(properties = {
        "app.seed.enabled=false",
        "app.jwt.secret=integration-test-secret-with-more-than-32-characters",
        "debug=false",
        "logging.level.root=INFO",
        "spring.jpa.show-sql=false"
})
@AutoConfigureMockMvc
class ImovelPublicIntegrationTest {

    @Container
    static final MySQLContainer MYSQL = new MySQLContainer("mysql:8.4")
            .withDatabaseName("imobiliaria_test")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Flyway flyway;

    @Autowired
    private ImovelRepository imovelRepository;

    @Autowired
    private ImagemImovelRepository imagemImovelRepository;

    @DynamicPropertySource
    static void configurarDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
    }

    @BeforeEach
    void setUp() {
        imagemImovelRepository.deleteAll();
        imovelRepository.deleteAll();
    }

    @Test
    void deveAplicarMigrationInicialComFlyway() {
        assertThat(flyway.info().current().getVersion().getVersion()).isEqualTo("1");
    }

    @Test
    void deveListarImovelPublicadoComImagemUsandoMysqlReal() throws Exception {
        Imovel imovel = novoImovelPublicado();
        Imovel imovelSalvo = imovelRepository.save(imovel);

        ImagemImovel imagem = new ImagemImovel();
        imagem.setImovel(imovelSalvo);
        imagem.setUrl("https://example.com/fachada.jpg");
        imagem.setOrdem(0);
        imagem.setCapa(true);
        imagemImovelRepository.save(imagem);

        mockMvc.perform(get("/api/imoveis")
                        .param("page", "0")
                        .param("size", "12")
                        .param("sort", "criadoEm")
                        .param("direction", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(imovelSalvo.getId()))
                .andExpect(jsonPath("$.content[0].titulo").value("Casa integrada com quintal"))
                .andExpect(jsonPath("$.content[0].status").value("PUBLICADO"))
                .andExpect(jsonPath("$.content[0].imagens[0].url").value("https://example.com/fachada.jpg"))
                .andExpect(jsonPath("$.content[0].imagens[0].capa").value(true));
    }

    private Imovel novoImovelPublicado() {
        Imovel imovel = new Imovel();
        imovel.setTitulo("Casa integrada com quintal");
        imovel.setDescricao("Casa com ambientes integrados e boa iluminacao.");
        imovel.setPreco(new BigDecimal("650000.00"));
        imovel.setTipo(TipoImovel.CASA);
        imovel.setCidade("Presidente Prudente");
        imovel.setBairro("Centro");
        imovel.setEndereco("Rua das Flores, 100");
        imovel.setQuartos(3);
        imovel.setBanheiros(2);
        imovel.setVagas(2);
        imovel.setArea(new BigDecimal("180.00"));
        imovel.setStatus(StatusImovel.PUBLICADO);

        return imovel;
    }
}
