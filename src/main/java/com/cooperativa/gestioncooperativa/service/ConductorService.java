package com.cooperativa.gestioncooperativa.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cooperativa.gestioncooperativa.entity.Conductor;
import com.cooperativa.gestioncooperativa.entity.Cooperativa;
import com.cooperativa.gestioncooperativa.repository.ConductorRepository;
import com.cooperativa.gestioncooperativa.repository.CooperativaRepository;

@Service
public class ConductorService {

    private final ConductorRepository conductorRepository;
    private final CooperativaRepository cooperativaRepository;

    public ConductorService(ConductorRepository conductorRepository, CooperativaRepository cooperativaRepository) {
        this.conductorRepository = conductorRepository;
        this.cooperativaRepository = cooperativaRepository;
    }

    public List<Conductor> listarConductores() {
        return conductorRepository.findAll();
    }

    public List<Conductor> listarPorCooperativa(Long idCooperativa) {
        return conductorRepository.findAllByCooperativa_IdCooperativa(idCooperativa);
    }

    @Transactional
    public Conductor guardarConductor(Conductor conductor) {
        if (conductor == null) {
            throw new IllegalArgumentException("El conductor es obligatorio.");
        }
        String cedula = texto(conductor.getCedula());
        String nombres = texto(conductor.getNombres());
        String apellidos = texto(conductor.getApellidos());
        String licencia = texto(conductor.getLicencia());
        String telefono = texto(conductor.getTelefono());
        String correo = texto(conductor.getCorreo());
        if (!cedulaValida(cedula)) {
            throw new IllegalArgumentException("La cédula ecuatoriana no es válida.");
        }
        if (nombres.isBlank() || apellidos.isBlank() || licencia.isBlank()) {
            throw new IllegalArgumentException("Nombres, apellidos y número de licencia son obligatorios.");
        }
        if (conductor.getFechaCaducidadLicencia() == null
                || conductor.getFechaCaducidadLicencia().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La licencia debe tener una fecha de caducidad vigente.");
        }
        if (!telefono.matches("^[0-9+()\\-\\s]{7,20}$")) {
            throw new IllegalArgumentException("El teléfono tiene un formato inválido.");
        }
        if (!correo.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("El correo electrónico no tiene un formato válido.");
        }
        if (conductorRepository.findAllByCedula(cedula).stream()
                .anyMatch(c -> !Objects.equals(c.getIdConductor(), conductor.getIdConductor()))) {
            throw new IllegalArgumentException("La cédula ya está registrada.");
        }
        conductor.setCedula(cedula);
        conductor.setNombres(nombres);
        conductor.setApellidos(apellidos);
        conductor.setLicencia(licencia);
        conductor.setTelefono(telefono);
        conductor.setCorreo(correo);
        String estado = texto(conductor.getEstado()).toUpperCase();
        if (!"ACTIVO".equals(estado) && !"INACTIVO".equals(estado)) {
            throw new IllegalArgumentException("El estado del conductor debe ser ACTIVO o INACTIVO.");
        }
        conductor.setEstado(estado);
        conductor.setCooperativa(obtenerCooperativaActiva(conductor.getCooperativa()));
        if (conductor.getIdConductor() != null && !conductorRepository.existsById(conductor.getIdConductor())) {
            throw new IllegalArgumentException("El conductor no existe.");
        }
        return conductorRepository.save(conductor);
    }

    public Conductor obtenerPorId(Long id) {
        return conductorRepository.findById(id).orElse(null);
    }

    @Transactional
    public void eliminarConductor(Long id) {
        conductorRepository.delete(conductorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("El conductor no existe.")));
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

    private boolean cedulaValida(String cedula) {
        if (!cedula.matches("\\d{10}")) {
            return false;
        }
        int provincia = Integer.parseInt(cedula.substring(0, 2));
        if (provincia < 1 || provincia > 24 || cedula.charAt(2) >= '6') {
            return false;
        }
        int suma = 0;
        for (int i = 0; i < 9; i++) {
            int digito = cedula.charAt(i) - '0';
            if (i % 2 == 0) {
                digito *= 2;
                if (digito > 9) {
                    digito -= 9;
                }
            }
            suma += digito;
        }
        int verificador = (10 - suma % 10) % 10;
        return verificador == cedula.charAt(9) - '0';
    }

    private String texto(String valor) {
        return valor == null ? "" : valor.trim();
    }
}
