package com.lorenzo.imobiliaria_api.auth.dto;

import com.lorenzo.imobiliaria_api.usuario.RoleUsuario;

public record LoginResponse(
        Long id,
        String nome,
        String email,
        RoleUsuario role,
        String token,
        String tokenType,
        Long expiresIn
) {
}
