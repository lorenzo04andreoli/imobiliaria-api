package com.lorenzo.imobiliaria_api.lead;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeadRepository extends JpaRepository<Lead, Long> {

    List<Lead> findAllByOrderByCriadoEmDesc();

    List<Lead> findByStatusOrderByCriadoEmDesc(StatusLead status);
}
