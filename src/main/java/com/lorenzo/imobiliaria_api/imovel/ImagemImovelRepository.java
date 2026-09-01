package com.lorenzo.imobiliaria_api.imovel;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImagemImovelRepository extends JpaRepository<ImagemImovel, Long> {

    List<ImagemImovel> findByImovelIdOrderByOrdemAsc(Long imovelId);

    long countByImovelId(Long imovelId);
}
