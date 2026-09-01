package com.lorenzo.imobiliaria_api.imovel;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImovelRepository extends JpaRepository<Imovel, Long> {

    List<Imovel> findByStatus(StatusImovel status);
}
