package com.cooperativa.gestioncooperativa.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cooperativa.gestioncooperativa.entity.Autobus;

public interface AutobusRepository extends JpaRepository<Autobus, Long> {

    java.util.List<Autobus> findAllByCooperativa_IdCooperativa(Long idCooperativa);

    java.util.List<Autobus> findAllByPlacaIgnoreCase(String placa);
}
