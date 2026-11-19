package com.cooperativa.gestioncooperativa.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "conductor")
@Data
public class Conductor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_conductor")
    private Long idConductor;

    @Column(unique = true)
    private String cedula;

    private String nombres;

    private String apellidos;

    private String licencia;

    private LocalDate fechaCaducidadLicencia;

    private String telefono;

    private String correo;

    private String estado;

    @ManyToOne
    @JoinColumn(name = "id_cooperativa")
    private Cooperativa cooperativa;
}
