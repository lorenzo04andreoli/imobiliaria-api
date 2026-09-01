package com.lorenzo.imobiliaria_api.config;

import com.lorenzo.imobiliaria_api.usuario.Usuario;
import com.lorenzo.imobiliaria_api.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.nome:}")
    private String adminNome;

    @Value("${app.admin.email:}")
    private String adminEmail;

    @Value("${app.admin.senha:}")
    private String adminSenha;

    @Override
    public void run(String... args) {
        if (!dadosAdminInformados()) {
            return;
        }

        usuarioRepository.findByEmail(adminEmail)
                .orElseGet(this::criarAdmin);
    }

    private boolean dadosAdminInformados() {
        return StringUtils.hasText(adminNome)
                && StringUtils.hasText(adminEmail)
                && StringUtils.hasText(adminSenha);
    }

    private Usuario criarAdmin() {
        Usuario usuario = new Usuario();
        usuario.setNome(adminNome);
        usuario.setEmail(adminEmail);
        usuario.setSenha(passwordEncoder.encode(adminSenha));

        return usuarioRepository.save(usuario);
    }
}
