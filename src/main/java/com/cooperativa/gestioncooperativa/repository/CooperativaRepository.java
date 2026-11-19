package com.cooperativa.gestioncooperativa.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cooperativa.gestioncooperativa.entity.Cooperativa;

public interface CooperativaRepository
        extends JpaRepository<Cooperativa, Long> {

    Optional<Cooperativa> findByRuc(String ruc);
}