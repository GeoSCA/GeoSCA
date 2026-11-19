package com.cooperativa.gestioncooperativa.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.cooperativa.gestioncooperativa.entity.Permiso;
import com.cooperativa.gestioncooperativa.repository.PermisoRepository;

@Service
public class PermisoService {

    private final PermisoRepository permisoRepository;

    public PermisoService(PermisoRepository permisoRepository) {
        this.permisoRepository = permisoRepository;
    }

    public List<Permiso> listarPermisos() {
        return permisoRepository.findAll();
    }
}
