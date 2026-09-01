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

    public LoginResponse login(LoginRequest request) {
        UsernamePasswordAuthenticationToken credentials =
                new UsernamePasswordAuthenticationToken(request.email(), request.senha());

        Usuario usuario = (Usuario) authenticationManager.authenticate(credentials).getPrincipal();

        return new LoginResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getRole()
        );
    }
}
