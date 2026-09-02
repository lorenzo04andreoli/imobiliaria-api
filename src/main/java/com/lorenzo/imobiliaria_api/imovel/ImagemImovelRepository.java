package com.lorenzo.imobiliaria_api.imovel;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ImagemImovelRepository extends JpaRepository<ImagemImovel, Long> {

    List<ImagemImovel> findByImovelIdOrderByOrdemAsc(Long imovelId);

    List<ImagemImovel> findByImovelIdInOrderByImovelIdAscOrdemAsc(List<Long> imovelIds);

    Optional<ImagemImovel> findByIdAndImovelId(Long id, Long imovelId);

    long countByImovelId(Long imovelId);
}
