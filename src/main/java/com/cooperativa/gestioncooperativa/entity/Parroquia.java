package com.cooperativa.gestioncooperativa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "parroquia")
@Data
public class Parroquia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_parroquia")
    private Long idParroquia;

    @Column(unique = true)
    private String nombre;

    private String canton;

    private String provincia;

    private String estado;
}
