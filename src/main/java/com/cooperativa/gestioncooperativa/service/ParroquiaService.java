package com.cooperativa.gestioncooperativa.service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.cooperativa.gestioncooperativa.entity.Parroquia;
import com.cooperativa.gestioncooperativa.repository.ParroquiaRepository;

@Service
public class ParroquiaService {

    private final ParroquiaRepository parroquiaRepository;

    public ParroquiaService(ParroquiaRepository parroquiaRepository) {
        this.parroquiaRepository = parroquiaRepository;
    }

    public List<Parroquia> listarParroquias() {
        return parroquiaRepository.findAll();
    }

    public List<Parroquia> listarParroquiasActivas() {
        return parroquiaRepository.findAll().stream()
                .filter(this::estaActiva)
                .collect(Collectors.toList());
    }

    public Parroquia guardarParroquia(Parroquia parroquia) {
        if (parroquia == null) {
            throw new IllegalArgumentException("La parroquia no puede ser nula.");
        }

        String nombre = normalizarTexto(parroquia.getNombre());
        String canton = normalizarTexto(parroquia.getCanton());
        String provincia = normalizarTexto(parroquia.getProvincia());

        if (nombre.isBlank() || canton.isBlank() || provincia.isBlank()) {
            throw new IllegalArgumentException("Nombre, cantón y provincia son obligatorios.");
        }

        if (!"LATACUNGA".equalsIgnoreCase(canton) || !"COTOPAXI".equalsIgnoreCase(provincia)) {
            throw new IllegalArgumentException("Solo se permiten parroquias del cantón Latacunga, provincia de Cotopaxi.");
        }

        Optional<Parroquia> existente = parroquiaRepository.findByNombreIgnoreCase(nombre);
        if (existente.isPresent() && !Objects.equals(existente.get().getIdParroquia(), parroquia.getIdParroquia())) {
            throw new IllegalArgumentException("Ya existe una parroquia con ese nombre.");
        }

        parroquia.setNombre(nombre);
        parroquia.setCanton(canton);
        parroquia.setProvincia(provincia);
        String estado = normalizarTexto(parroquia.getEstado()).toUpperCase();
        if (estado.isBlank()) {
            estado = "ACTIVA";
        }
        if (!"ACTIVA".equals(estado) && !"INACTIVA".equals(estado)) {
            throw new IllegalArgumentException("El estado de la parroquia debe ser ACTIVA o INACTIVA.");
        }
        parroquia.setEstado(estado);

        return parroquiaRepository.save(parroquia);
    }

    public Parroquia obtenerPorId(Long id) {
        return parroquiaRepository.findById(id).orElse(null);
    }

    public void eliminarParroquia(Long id) {
        Parroquia parroquia = parroquiaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("La parroquia no existe."));
        parroquia.setEstado("INACTIVA");
        parroquiaRepository.save(parroquia);
    }

    public void activarParroquia(Long id) {
        Parroquia parroquia = parroquiaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("La parroquia no existe."));
        parroquia.setEstado("ACTIVA");
        parroquiaRepository.save(parroquia);
    }

    public void inactivarParroquia(Long id) {
        Parroquia parroquia = parroquiaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("La parroquia no existe."));
        parroquia.setEstado("INACTIVA");
        parroquiaRepository.save(parroquia);
    }

    public boolean estaActiva(Parroquia parroquia) {
        if (parroquia == null) {
            return false;
        }
        String estado = normalizarTexto(parroquia.getEstado());
        return estado.isEmpty() || "ACTIVA".equalsIgnoreCase(estado) || "ACTIVO".equalsIgnoreCase(estado);
    }

    private String normalizarTexto(String valor) {
        if (valor == null) {
            return "";
        }
        return valor.trim();
    }
}
