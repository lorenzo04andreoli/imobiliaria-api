package com.lorenzo.imobiliaria_api.auth.service;

import com.lorenzo.imobiliaria_api.auth.dto.LoginRequest;
import com.lorenzo.imobiliaria_api.auth.dto.LoginResponse;
import com.lorenzo.imobiliaria_api.usuario.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    public LoginResponse login(LoginRequest request) {
        UsernamePasswordAuthenticationToken credentials =
                new UsernamePasswordAuthenticationToken(request.email(), request.senha());

        Usuario usuario = (Usuario) authenticationManager.authenticate(credentials).getPrincipal();
        String token = tokenService.gerarToken(usuario);

        return new LoginResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getRole(),
                token,
                "Bearer",
                tokenService.getExpirationSeconds()
        );
    }
}
