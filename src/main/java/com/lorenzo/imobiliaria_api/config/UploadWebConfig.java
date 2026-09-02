package com.lorenzo.imobiliaria_api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class UploadWebConfig implements WebMvcConfigurer {

    @Value("${app.upload.imoveis-dir:uploads/imoveis}")
    private String imoveisDir;

    @Value("${app.upload.public-path:/uploads/imoveis}")
    private String publicPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = Path.of(imoveisDir).toAbsolutePath().normalize().toUri().toString();
        registry.addResourceHandler(normalizarPublicPath() + "/**")
                .addResourceLocations(location);
    }

    private String normalizarPublicPath() {
        return publicPath.startsWith("/") ? publicPath : "/" + publicPath;
    }
}
