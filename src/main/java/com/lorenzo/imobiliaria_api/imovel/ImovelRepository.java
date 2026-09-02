package com.lorenzo.imobiliaria_api.imovel;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ImovelRepository extends JpaRepository<Imovel, Long> {

    List<Imovel> findByStatus(StatusImovel status);

    List<Imovel> findAllByOrderByCriadoEmDesc();

    List<Imovel> findByStatusOrderByCriadoEmDesc(StatusImovel status);

    Optional<Imovel> findByIdAndStatus(Long id, StatusImovel status);

    @Query("""
            select i
            from Imovel i
            where i.status = :status
              and (:q is null or lower(i.titulo) like lower(concat('%', :q, '%'))
                   or lower(i.descricao) like lower(concat('%', :q, '%'))
                   or lower(i.cidade) like lower(concat('%', :q, '%'))
                   or lower(i.bairro) like lower(concat('%', :q, '%')))
              and (:cidade is null or lower(i.cidade) like lower(concat('%', :cidade, '%')))
              and (:bairro is null or lower(i.bairro) like lower(concat('%', :bairro, '%')))
              and (:tipo is null or i.tipo = :tipo)
              and (:precoMin is null or i.preco >= :precoMin)
              and (:precoMax is null or i.preco <= :precoMax)
              and (:quartosMin is null or i.quartos >= :quartosMin)
              and (:banheirosMin is null or i.banheiros >= :banheirosMin)
              and (:vagasMin is null or i.vagas >= :vagasMin)
            """)
    Page<Imovel> buscarPublicadosComFiltros(
            @Param("status") StatusImovel status,
            @Param("q") String q,
            @Param("cidade") String cidade,
            @Param("bairro") String bairro,
            @Param("tipo") TipoImovel tipo,
            @Param("precoMin") BigDecimal precoMin,
            @Param("precoMax") BigDecimal precoMax,
            @Param("quartosMin") Integer quartosMin,
            @Param("banheirosMin") Integer banheirosMin,
            @Param("vagasMin") Integer vagasMin,
            Pageable pageable
    );
}
