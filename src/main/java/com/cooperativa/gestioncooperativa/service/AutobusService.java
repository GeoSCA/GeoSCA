package com.cooperativa.gestioncooperativa.service;

import java.time.Year;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cooperativa.gestioncooperativa.entity.Autobus;
import com.cooperativa.gestioncooperativa.entity.Cooperativa;
import com.cooperativa.gestioncooperativa.repository.AutobusRepository;
import com.cooperativa.gestioncooperativa.repository.CooperativaRepository;

@Service
public class AutobusService {

    private final AutobusRepository autobusRepository;
    private final CooperativaRepository cooperativaRepository;

    public AutobusService(AutobusRepository autobusRepository, CooperativaRepository cooperativaRepository) {
        this.autobusRepository = autobusRepository;
        this.cooperativaRepository = cooperativaRepository;
    }

    public List<Autobus> listarAutobuses() {
        return autobusRepository.findAll();
    }

    public List<Autobus> listarPorCooperativa(Long idCooperativa) {
        return autobusRepository.findAllByCooperativa_IdCooperativa(idCooperativa);
    }

    @Transactional
    public Autobus guardarAutobus(Autobus autobus) {
        if (autobus == null) {
            throw new IllegalArgumentException("El autobús es obligatorio.");
        }
        String placa = texto(autobus.getPlaca()).toUpperCase();
        String marca = texto(autobus.getMarca());
        String modelo = texto(autobus.getModelo());
        if (!placa.matches("^[A-Z]{3}-\\d{3,4}$")) {
            throw new IllegalArgumentException("La placa debe tener formato ecuatoriano, por ejemplo ABC-1234.");
        }
        if (marca.isBlank() || modelo.isBlank()) {
            throw new IllegalArgumentException("La marca y el modelo son obligatorios.");
        }
        int anioActual = Year.now().getValue();
        if (autobus.getAnio() == null || autobus.getAnio() < 1980 || autobus.getAnio() > anioActual + 1) {
            throw new IllegalArgumentException("El año debe estar entre 1980 y " + (anioActual + 1) + ".");
        }
        if (autobus.getCapacidad() == null || autobus.getCapacidad() <= 0) {
            throw new IllegalArgumentException("La capacidad debe ser mayor que cero.");
        }
        List<Autobus> duplicados = autobusRepository.findAllByPlacaIgnoreCase(placa);
        if (duplicados.stream().anyMatch(a -> !Objects.equals(a.getIdAutobus(), autobus.getIdAutobus()))) {
            throw new IllegalArgumentException("La placa ya está registrada.");
        }
        autobus.setPlaca(placa);
        autobus.setMarca(marca);
        autobus.setModelo(modelo);
        autobus.setEstado(normalizarEstado(autobus.getEstado()));
        autobus.setCooperativa(obtenerCooperativaActiva(autobus.getCooperativa()));
        if (autobus.getIdAutobus() != null && !autobusRepository.existsById(autobus.getIdAutobus())) {
            throw new IllegalArgumentException("El autobús no existe.");
        }
        return autobusRepository.save(autobus);
    }

    public Autobus actualizarEstado(Long id, String estado) {
        Autobus autobus = obtenerExistente(id);
        autobus.setEstado(normalizarEstado(estado));
        return autobusRepository.save(autobus);
    }

    public Autobus obtenerPorId(Long id) {
        return autobusRepository.findById(id).orElse(null);
    }

    @Transactional
    public void eliminarAutobus(Long id) {
        autobusRepository.delete(obtenerExistente(id));
    }

    private Autobus obtenerExistente(Long id) {
        return autobusRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("El autobús no existe."));
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

    private String normalizarEstado(String estado) {
        String valor = texto(estado).toUpperCase();
        if (!"ACTIVO".equals(valor) && !"INACTIVO".equals(valor)) {
            throw new IllegalArgumentException("El estado del autobús debe ser ACTIVO o INACTIVO.");
        }
        return valor;
    }

    private String texto(String valor) {
        return valor == null ? "" : valor.trim();
    }
}
