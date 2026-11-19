package com.cooperativa.gestioncooperativa.service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.cooperativa.gestioncooperativa.entity.Cooperativa;
import com.cooperativa.gestioncooperativa.repository.CooperativaRepository;

@Service
public class CooperativaService {

    private final CooperativaRepository cooperativaRepository;

    public CooperativaService(CooperativaRepository cooperativaRepository) {
        this.cooperativaRepository = cooperativaRepository;
    }

    public List<Cooperativa> listarCooperativas() {
        return cooperativaRepository.findAll();
    }

    public List<Cooperativa> listarCooperativasActivas() {
        return cooperativaRepository.findAll().stream()
                .filter(this::estaActiva)
                .collect(Collectors.toList());
    }

    public Cooperativa guardarCooperativa(Cooperativa cooperativa) {
        if (cooperativa == null) {
            throw new IllegalArgumentException("La cooperativa no puede ser nula.");
        }

        String nombre = normalizarTexto(cooperativa.getNombre());
        String ruc = normalizarTexto(cooperativa.getRuc());
        String direccion = normalizarTexto(cooperativa.getDireccion());
        String telefono = normalizarTexto(cooperativa.getTelefono());
        String correo = normalizarTexto(cooperativa.getCorreo());

        if (nombre.isBlank() || ruc.isBlank() || direccion.isBlank() || telefono.isBlank() || correo.isBlank()) {
            throw new IllegalArgumentException("Todos los campos de la cooperativa son obligatorios.");
        }

        if (!ruc.matches("\\d{13}")) {
            throw new IllegalArgumentException("El RUC debe contener 13 dígitos numéricos.");
        }

        Optional<Cooperativa> existente = cooperativaRepository.findByRuc(ruc);
        if (existente.isPresent() && !Objects.equals(existente.get().getIdCooperativa(), cooperativa.getIdCooperativa())) {
            throw new IllegalArgumentException("El RUC ya está registrado en otra cooperativa.");
        }

        if (!correo.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")) {
            throw new IllegalArgumentException("El correo electrónico no tiene un formato válido.");
        }

        if (!telefono.matches("^[0-9+()\\-\\s]{7,20}$")) {
            throw new IllegalArgumentException("El teléfono tiene un formato inválido.");
        }

        cooperativa.setNombre(nombre);
        cooperativa.setRuc(ruc);
        cooperativa.setDireccion(direccion);
        cooperativa.setTelefono(telefono);
        cooperativa.setCorreo(correo);

        if (cooperativa.getEstado() == null || cooperativa.getEstado().isBlank()) {
            cooperativa.setEstado("ACTIVA");
        } else {
            String estado = cooperativa.getEstado().trim().toUpperCase();
            if (!"ACTIVA".equals(estado) && !"INACTIVA".equals(estado)) {
                throw new IllegalArgumentException("El estado de la cooperativa debe ser ACTIVA o INACTIVA.");
            }
            cooperativa.setEstado(estado);
        }

        return cooperativaRepository.save(cooperativa);
    }

    public Cooperativa obtenerPorId(Long id) {
        return cooperativaRepository.findById(id).orElse(null);
    }

    public void inactivarCooperativa(Long id) {
        Cooperativa cooperativa = cooperativaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("La cooperativa no existe."));
        cooperativa.setEstado("INACTIVA");
        cooperativaRepository.save(cooperativa);
    }

    public void activarCooperativa(Long id) {
        Cooperativa cooperativa = cooperativaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("La cooperativa no existe."));
        cooperativa.setEstado("ACTIVA");
        cooperativaRepository.save(cooperativa);
    }

    public boolean estaActiva(Cooperativa cooperativa) {
        if (cooperativa == null) {
            return false;
        }
        String estado = normalizarTexto(cooperativa.getEstado());
        return estado.isEmpty() || "ACTIVA".equalsIgnoreCase(estado) || "ACTIVO".equalsIgnoreCase(estado);
    }

    private String normalizarTexto(String valor) {
        if (valor == null) {
            return "";
        }
        return valor.trim();
    }
}
