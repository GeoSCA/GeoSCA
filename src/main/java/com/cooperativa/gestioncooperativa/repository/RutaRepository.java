package com.cooperativa.gestioncooperativa.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cooperativa.gestioncooperativa.entity.Ruta;

public interface RutaRepository extends JpaRepository<Ruta, Long> {

    java.util.List<Ruta> findAllByCooperativa_IdCooperativa(Long idCooperativa);

    java.util.List<Ruta> findAllByNombreIgnoreCaseAndCooperativa_IdCooperativa(String nombre, Long idCooperativa);
}
