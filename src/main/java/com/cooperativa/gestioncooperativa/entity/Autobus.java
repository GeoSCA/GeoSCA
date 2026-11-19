package com.cooperativa.gestioncooperativa.entity;

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
@Table(name = "autobus")
@Data
public class Autobus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_autobus")
    private Long idAutobus;

    @Column(unique = true)
    private String placa;

    private String marca;

    private String modelo;

    private Integer anio;

    private Integer capacidad;

    private String estado;

    @ManyToOne
    @JoinColumn(name = "id_cooperativa")
    private Cooperativa cooperativa;
}
