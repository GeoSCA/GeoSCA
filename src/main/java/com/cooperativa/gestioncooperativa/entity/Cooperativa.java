package com.cooperativa.gestioncooperativa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "cooperativa")
@Data
public class Cooperativa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cooperativa")
    private Long idCooperativa;

    private String nombre;

    @Column(unique = true)
    private String ruc;

    private String direccion;

    private String telefono;

    private String correo;

    private String estado;
}