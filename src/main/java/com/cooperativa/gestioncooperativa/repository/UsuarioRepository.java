package com.cooperativa.gestioncooperativa.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cooperativa.gestioncooperativa.entity.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);

    List<Usuario> findAllByUsernameIgnoreCase(String username);

    List<Usuario> findAllByCorreoIgnoreCase(String correo);

    boolean existsByRol_IdRol(Long idRol);
}