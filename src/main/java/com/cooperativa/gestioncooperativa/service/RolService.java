package com.cooperativa.gestioncooperativa.service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cooperativa.gestioncooperativa.entity.Permiso;
import com.cooperativa.gestioncooperativa.entity.Rol;
import com.cooperativa.gestioncooperativa.repository.PermisoRepository;
import com.cooperativa.gestioncooperativa.repository.RolRepository;
import com.cooperativa.gestioncooperativa.repository.UsuarioRepository;

@Service
public class RolService {

    private static final Set<String> ROLES_BASE = Set.of("ADMIN_GENERAL", "ADMIN_COOPERATIVA");

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final PermisoRepository permisoRepository;

    public RolService(
            RolRepository rolRepository,
            UsuarioRepository usuarioRepository,
            PermisoRepository permisoRepository) {
        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
        this.permisoRepository = permisoRepository;
    }

    public List<Rol> listarRoles() {
        return rolRepository.findAll();
    }

    public Optional<Rol> obtenerPorId(Long id) {
        return rolRepository.findById(id);
    }

    @Transactional
    public Rol guardarRol(Rol rol) {
        if (rol == null) {
            throw new IllegalArgumentException("El rol no puede ser nulo.");
        }
        String nombre = texto(rol.getNombre());
        if (nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del rol es obligatorio.");
        }
        Rol actual = null;
        if (rol.getIdRol() != null) {
            actual = rolRepository.findById(rol.getIdRol())
                    .orElseThrow(() -> new IllegalArgumentException("El rol no existe."));
            if (esRolBase(actual.getNombre()) && !actual.getNombre().equalsIgnoreCase(nombre)) {
                throw new IllegalArgumentException("No se puede renombrar un rol base del sistema.");
            }
        }
        Optional<Rol> duplicado = rolRepository.findByNombreIgnoreCase(nombre);
        if (duplicado.isPresent() && !Objects.equals(duplicado.get().getIdRol(), rol.getIdRol())) {
            throw new IllegalArgumentException("Ya existe un rol con ese nombre.");
        }

        Rol guardar = actual == null ? rol : actual;
        guardar.setNombre(nombre);
        guardar.setDescripcion(texto(rol.getDescripcion()));
        if (actual == null || !"ADMIN_GENERAL".equalsIgnoreCase(actual.getNombre())) {
            Set<Long> permisoIds = rol.getPermisos() == null ? Set.of()
                    : rol.getPermisos().stream()
                            .map(Permiso::getIdPermiso)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet());
            Set<Permiso> permisos = new LinkedHashSet<>(permisoRepository.findAllById(permisoIds));
            if (permisos.size() != permisoIds.size()) {
                throw new IllegalArgumentException("Uno de los permisos seleccionados no existe.");
            }
            guardar.setPermisos(permisos);
        }
        return rolRepository.save(guardar);
    }

    public void eliminarRol(Long id) {
        Rol rol = rolRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("El rol no existe."));
        if (esRolBase(rol.getNombre())) {
            throw new IllegalArgumentException("No se pueden eliminar los roles base del sistema.");
        }
        if (usuarioRepository.existsByRol_IdRol(rol.getIdRol())) {
            throw new IllegalArgumentException("No se puede eliminar un rol que tiene usuarios asociados.");
        }
        rolRepository.delete(rol);
    }

    private boolean esRolBase(String nombre) {
        return nombre != null && ROLES_BASE.contains(nombre.toUpperCase());
    }

    private String texto(String valor) {
        return valor == null ? "" : valor.trim();
    }
}
