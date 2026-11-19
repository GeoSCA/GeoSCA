package com.cooperativa.gestioncooperativa.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cooperativa.gestioncooperativa.entity.Conductor;

public interface ConductorRepository extends JpaRepository<Conductor, Long> {

    java.util.List<Conductor> findAllByCooperativa_IdCooperativa(Long idCooperativa);

    java.util.List<Conductor> findAllByCedula(String cedula);
}
