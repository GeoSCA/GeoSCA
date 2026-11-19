package com.cooperativa.gestioncooperativa.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cooperativa.gestioncooperativa.entity.Parroquia;

public interface ParroquiaRepository extends JpaRepository<Parroquia, Long> {

    Optional<Parroquia> findByNombreIgnoreCase(String nombre);

    java.util.List<Parroquia> findAllByEstadoIgnoreCase(String estado);
}