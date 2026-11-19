package com.cooperativa.gestioncooperativa.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cooperativa.gestioncooperativa.entity.Rol;

public interface RolRepository extends JpaRepository<Rol, Long> {

    Optional<Rol> findByNombreIgnoreCase(String nombre);
}