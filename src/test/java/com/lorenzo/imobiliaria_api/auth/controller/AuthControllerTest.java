package com.lorenzo.imobiliaria_api.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lorenzo.imobiliaria_api.auth.dto.LoginRequest;
import com.lorenzo.imobiliaria_api.auth.dto.LoginResponse;
import com.lorenzo.imobiliaria_api.auth.service.AuthService;
import com.lorenzo.imobiliaria_api.exception.GlobalExceptionHandler;
import com.lorenzo.imobiliaria_api.usuario.RoleUsuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest {

    private AuthService authService;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AuthController(authService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void deveRealizarLoginAdministrativo() throws Exception {
        LoginResponse response = new LoginResponse(
                1L,
                "Administrador",
                "admin@seudominio.com",
                RoleUsuario.ADMIN,
                "jwt-token",
                "Bearer",
                7200L
        );

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(
                                "admin@seudominio.com",
                                "senha-local-dev"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Administrador"))
                .andExpect(jsonPath("$.email").value("admin@seudominio.com"))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(7200));

        ArgumentCaptor<LoginRequest> requestCaptor = ArgumentCaptor.forClass(LoginRequest.class);
        verify(authService).login(requestCaptor.capture());

        LoginRequest request = requestCaptor.getValue();
        assertThat(request.email()).isEqualTo("admin@seudominio.com");
        assertThat(request.senha()).isEqualTo("senha-local-dev");
    }

    @Test
    void deveRetornarNaoAutorizadoQuandoCredenciaisForemInvalidas() throws Exception {
        when(authService.login(any(LoginRequest.class))).thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(
                                "admin@seudominio.com",
                                "senha-incorreta"
                        ))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Email ou senha invalidos"))
                .andExpect(jsonPath("$.path").value("/api/auth/login"));
    }

    @Test
    void deveValidarPayloadDoLogin() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(
                                "email-invalido",
                                ""
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Dados invalidos"))
                .andExpect(jsonPath("$.path").value("/api/auth/login"))
                .andExpect(jsonPath("$.fields").isArray());
    }
}
