package com.lorenzo.imobiliaria_api.imovel;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ImovelRepository extends JpaRepository<Imovel, Long> {

    List<Imovel> findByStatus(StatusImovel status);

    List<Imovel> findAllByOrderByCriadoEmDesc();

    List<Imovel> findByStatusOrderByCriadoEmDesc(StatusImovel status);

    Optional<Imovel> findByIdAndStatus(Long id, StatusImovel status);
}
