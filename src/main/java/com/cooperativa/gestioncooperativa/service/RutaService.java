package com.cooperativa.gestioncooperativa.service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cooperativa.gestioncooperativa.entity.Cooperativa;
import com.cooperativa.gestioncooperativa.entity.Parroquia;
import com.cooperativa.gestioncooperativa.entity.Ruta;
import com.cooperativa.gestioncooperativa.repository.CooperativaRepository;
import com.cooperativa.gestioncooperativa.repository.ParroquiaRepository;
import com.cooperativa.gestioncooperativa.repository.RutaRepository;

@Service
public class RutaService {

    private final RutaRepository rutaRepository;
    private final CooperativaRepository cooperativaRepository;
    private final ParroquiaRepository parroquiaRepository;

    public RutaService(
            RutaRepository rutaRepository,
            CooperativaRepository cooperativaRepository,
            ParroquiaRepository parroquiaRepository) {
        this.rutaRepository = rutaRepository;
        this.cooperativaRepository = cooperativaRepository;
        this.parroquiaRepository = parroquiaRepository;
    }

    public List<Ruta> listarRutas() {
        return rutaRepository.findAll();
    }

    public List<Ruta> listarPorCooperativa(Long idCooperativa) {
        return rutaRepository.findAllByCooperativa_IdCooperativa(idCooperativa);
    }

    @Transactional
    public Ruta guardarRuta(Ruta ruta) {
        if (ruta == null) {
            throw new IllegalArgumentException("La ruta es obligatoria.");
        }
        String nombre = texto(ruta.getNombre());
        if (nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la ruta es obligatorio.");
        }
        Cooperativa cooperativa = obtenerCooperativaActiva(ruta.getCooperativa());
        List<Ruta> rutasConNombre = rutaRepository.findAllByNombreIgnoreCaseAndCooperativa_IdCooperativa(
                nombre, cooperativa.getIdCooperativa());
        if (rutasConNombre.stream().anyMatch(r -> !Objects.equals(r.getIdRuta(), ruta.getIdRuta()))) {
            throw new IllegalArgumentException("Ya existe una ruta con ese nombre en la cooperativa.");
        }

        Parroquia origen = resolverParroquia(ruta.getParroquiaOrigen(), ruta.getOrigen(), "origen");
        Parroquia destino = resolverParroquia(ruta.getParroquiaDestino(), ruta.getDestino(), "destino");
        if (Objects.equals(origen.getIdParroquia(), destino.getIdParroquia())) {
            throw new IllegalArgumentException("El origen y el destino deben ser distintos.");
        }
        if (ruta.getDistanciaKm() != null && ruta.getDistanciaKm() <= 0) {
            throw new IllegalArgumentException("La distancia debe ser mayor que cero.");
        }

        Set<Parroquia> paradas = new LinkedHashSet<>();
        if (ruta.getParadas() != null) {
            for (Parroquia parada : ruta.getParadas()) {
                if (parada != null && parada.getIdParroquia() != null) {
                    Parroquia activa = parroquiaRepository.findById(parada.getIdParroquia())
                            .orElseThrow(() -> new IllegalArgumentException("Una parroquia intermedia no existe."));
                    validarParroquiaActiva(activa, "intermedia");
                    if (!Objects.equals(activa.getIdParroquia(), origen.getIdParroquia())
                            && !Objects.equals(activa.getIdParroquia(), destino.getIdParroquia())) {
                        paradas.add(activa);
                    }
                }
            }
        }

        ruta.setNombre(nombre);
        ruta.setParroquiaOrigen(origen);
        ruta.setParroquiaDestino(destino);
        ruta.setOrigen(origen.getNombre());
        ruta.setDestino(destino.getNombre());
        ruta.setParadas(paradas);
        ruta.setCooperativa(cooperativa);
        String estado = texto(ruta.getEstado()).toUpperCase();
        if (!"ACTIVA".equals(estado) && !"INACTIVA".equals(estado)) {
            throw new IllegalArgumentException("El estado de la ruta debe ser ACTIVA o INACTIVA.");
        }
        ruta.setEstado(estado);
        if (ruta.getIdRuta() != null && !rutaRepository.existsById(ruta.getIdRuta())) {
            throw new IllegalArgumentException("La ruta no existe.");
        }
        return rutaRepository.save(ruta);
    }

    public Ruta obtenerPorId(Long id) {
        return rutaRepository.findById(id).orElse(null);
    }

    @Transactional
    public void eliminarRuta(Long id) {
        rutaRepository.delete(rutaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("La ruta no existe.")));
    }

    private Parroquia resolverParroquia(Parroquia referencia, String nombreAntiguo, String etiqueta) {
        Parroquia parroquia = null;
        if (referencia != null && referencia.getIdParroquia() != null) {
            parroquia = parroquiaRepository.findById(referencia.getIdParroquia())
                    .orElseThrow(() -> new IllegalArgumentException("La parroquia de " + etiqueta + " no existe."));
        } else if (!texto(nombreAntiguo).isBlank()) {
            parroquia = parroquiaRepository.findByNombreIgnoreCase(texto(nombreAntiguo))
                    .orElseThrow(() -> new IllegalArgumentException("Seleccione una parroquia válida para " + etiqueta + "."));
        }
        if (parroquia == null) {
            throw new IllegalArgumentException("La parroquia de " + etiqueta + " es obligatoria.");
        }
        validarParroquiaActiva(parroquia, etiqueta);
        return parroquia;
    }

    private void validarParroquiaActiva(Parroquia parroquia, String etiqueta) {
        String estado = texto(parroquia.getEstado());
        if (!estado.isBlank() && !"ACTIVA".equalsIgnoreCase(estado) && !"ACTIVO".equalsIgnoreCase(estado)) {
            throw new IllegalArgumentException("La parroquia de " + etiqueta + " está inactiva.");
        }
        if (!"LATACUNGA".equalsIgnoreCase(texto(parroquia.getCanton()))
                || !"COTOPAXI".equalsIgnoreCase(texto(parroquia.getProvincia()))) {
            throw new IllegalArgumentException("La parroquia de " + etiqueta + " no pertenece a Latacunga, Cotopaxi.");
        }
    }

    private Cooperativa obtenerCooperativaActiva(Cooperativa referencia) {
        if (referencia == null || referencia.getIdCooperativa() == null) {
            throw new IllegalArgumentException("Debe asignar una cooperativa.");
        }
        Cooperativa cooperativa = cooperativaRepository.findById(referencia.getIdCooperativa())
                .orElseThrow(() -> new IllegalArgumentException("La cooperativa seleccionada no existe."));
        String estado = texto(cooperativa.getEstado());
        if (!estado.isBlank() && !"ACTIVA".equalsIgnoreCase(estado) && !"ACTIVO".equalsIgnoreCase(estado)) {
            throw new IllegalArgumentException("La cooperativa seleccionada está inactiva.");
        }
        return cooperativa;
    }

    private String texto(String valor) {
        return valor == null ? "" : valor.trim();
    }
}
